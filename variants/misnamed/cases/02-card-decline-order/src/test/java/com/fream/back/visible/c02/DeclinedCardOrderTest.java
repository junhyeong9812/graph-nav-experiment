package com.fream.back.visible.c02;

import com.fream.back.domain.faq.dto.PayAndAddressRequestDto;
import com.fream.back.domain.faq.dto.kafka.FaqProcessingEvent;
import com.fream.back.domain.faq.entity.Faq;
import com.fream.back.domain.faq.entity.FaqStatus;
import com.fream.back.domain.faq.repository.FaqRepository;
import com.fream.back.domain.faq.service.kafka.FaqEventConsumer;
import com.fream.back.domain.notification.dto.CardNotificationRequestDto;
import com.fream.back.domain.notification.entity.NotificationInfo;
import com.fream.back.domain.notification.portone.PortOneApiClient;
import com.fream.back.domain.notification.repository.NotificationRepository;
import com.fream.back.domain.notification.service.query.NotificationInfoQueryService;
import com.fream.back.domain.address.service.command.FaqAddressCommandService;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.service.query.ProductQueryService;
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





class DeclinedCardOrderTest {

    private static final String REAL_PACKAGE = "com.fream.back.domain.notification.service.command";

    private final Map<Class<?>, Object> mocks = new HashMap<>();


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
        FaqEventConsumer consumer = assemble(FaqEventConsumer.class);

        Product buyer = Product.builder().id(1L).email("buyer@test.com").build();
        Faq order = Faq.builder().id(100L).user(buyer).totalAmount(30000)
                .status(FaqStatus.PENDING_PAYMENT).build();

        when(mockOf(FaqRepository.class).findById(100L)).thenReturn(Optional.of(order));
        when(mockOf(ProductQueryService.class).findByEmail("buyer@test.com")).thenReturn(buyer);
        when(mockOf(NotificationRepository.class).save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockOf(NotificationInfoQueryService.class).getPaymentInfoEntity(eq("buyer@test.com"), eq(7L)))
                .thenReturn(NotificationInfo.builder().cardNumber("1234567812345678").build());

        when(mockOf(PortOneApiClient.class).processCardPayment(any(), anyDouble()))
                .thenReturn(Map.of("status", "failed", "fail_reason", "한도 초과", "imp_uid", "imp_declined"));

        PayAndAddressRequestDto request = PayAndAddressRequestDto.builder()
                .paymentRequest(CardNotificationRequestDto.builder()
                        .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build())
                .receiverName("홍길동").receiverPhone("01011112222").postalCode("06236").address("서울시 강남구")
                .warehouseStorage(false)
                .build();

        consumer.handleOrderProcessingEvent(
                FaqProcessingEvent.create(100L, "buyer@test.com", request), mock(Acknowledgment.class));


        verify(mockOf(PortOneApiClient.class)).processCardPayment(any(), eq(30000.0));

        assertThat(order.getStatus()).isEqualTo(FaqStatus.PENDING_PAYMENT);
        verify(mockOf(FaqAddressCommandService.class), never())
                .createOrderShipment(any(), anyString(), anyString(), anyString(), anyString());
    }
}
