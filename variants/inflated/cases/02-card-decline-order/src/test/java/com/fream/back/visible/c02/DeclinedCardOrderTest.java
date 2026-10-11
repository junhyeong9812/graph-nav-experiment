package com.fream.back.visible.c02;

import com.fream.back.domain.order.dto.PayAndShipmentRequestDto;
import com.fream.back.domain.order.dto.kafka.OrderProcessingEvent;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderStatus;
import com.fream.back.domain.order.repository.OrderRepository;
import com.fream.back.domain.order.service.kafka.OrderEventConsumer;
import com.fream.back.domain.payment.dto.CardPaymentRequestDto;
import com.fream.back.domain.payment.entity.PaymentInfo;
import com.fream.back.domain.payment.portone.PortOneApiClient;
import com.fream.back.domain.payment.repository.PaymentRepository;
import com.fream.back.domain.payment.service.query.PaymentInfoQueryService;
import com.fream.back.domain.shipment.service.command.OrderShipmentCommandService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.service.query.UserQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.support.Acknowledgment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 주문 처리 이벤트(결제 + 배송)를 받았을 때, 카드사가 승인을 거절한 경우의 주문 처리.
 * 결제 명령 처리 계층은 실제 객체를 쓰고, 외부 PG·저장소·배송 등은 목으로 둔다.
 */
class DeclinedCardOrderTest {

    private static final String REAL_PACKAGE = "com.fream.back.domain.payment.service.command";

    private final Map<Class<?>, Object> mocks = new HashMap<>();

    /** 대상 클래스를 생성자 주입으로 만든다. REAL_PACKAGE의 구체 클래스는 실제로, 나머지는 목으로 채운다. */
    private <T> T assemble(Class<T> type) throws Exception {
        Constructor<?> c = Arrays.stream(type.getDeclaredConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount)).orElseThrow();
        Class<?>[] types = c.getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> t = types[i];
            boolean concrete = !t.isInterface() && !Modifier.isAbstract(t.getModifiers());
            args[i] = (concrete && t.getName().startsWith(REAL_PACKAGE))
                    ? assemble(t)
                    : mocks.computeIfAbsent(t, Mockito::mock);
        }
        return type.cast(c.newInstance(args));
    }

    @SuppressWarnings("unchecked")
    private <T> T mockOf(Class<T> type) {
        return (T) mocks.computeIfAbsent(type, Mockito::mock);
    }

    @Test
    void declinedCard_keepsOrderWaitingForPayment_andCreatesNoShipment() throws Exception {
        OrderEventConsumer consumer = assemble(OrderEventConsumer.class);

        User buyer = User.builder().id(1L).email("buyer@test.com").build();
        Order order = Order.builder().id(100L).user(buyer).totalAmount(30000)
                .status(OrderStatus.PENDING_PAYMENT).build();

        when(mockOf(OrderRepository.class).findById(100L)).thenReturn(Optional.of(order));
        when(mockOf(UserQueryService.class).findByEmail("buyer@test.com")).thenReturn(buyer);
        when(mockOf(PaymentRepository.class).save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockOf(PaymentInfoQueryService.class).getPaymentInfoEntity(eq("buyer@test.com"), eq(7L)))
                .thenReturn(PaymentInfo.builder().cardNumber("1234567812345678").build());
        // 카드사 응답: 승인 거절
        when(mockOf(PortOneApiClient.class).processCardPayment(any(), anyDouble()))
                .thenReturn(Map.of("status", "failed", "fail_reason", "한도 초과", "imp_uid", "imp_declined"));

        PayAndShipmentRequestDto request = PayAndShipmentRequestDto.builder()
                .paymentRequest(CardPaymentRequestDto.builder()
                        .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build())
                .receiverName("홍길동").receiverPhone("01011112222").postalCode("06236").address("서울시 강남구")
                .warehouseStorage(false)
                .build();

        consumer.handleOrderProcessingEvent(
                OrderProcessingEvent.create(100L, "buyer@test.com", request), mock(Acknowledgment.class));

        // 결제 요청은 실제로 카드사까지 갔다
        verify(mockOf(PortOneApiClient.class)).processCardPayment(any(), eq(30000.0));
        // 거절된 결제로는 주문이 진행되지 않아야 한다
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        verify(mockOf(OrderShipmentCommandService.class), never())
                .createOrderShipment(any(), anyString(), anyString(), anyString(), anyString());
    }
}
