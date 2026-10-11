package com.fream.back.hidden.c02;

import com.fream.back.domain.notification.service.command.NotificationCommandService;
import com.fream.back.domain.order.dto.PayAndShipmentRequestDto;
import com.fream.back.domain.order.dto.kafka.OrderProcessingEvent;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderStatus;
import com.fream.back.domain.order.repository.OrderRepository;
import com.fream.back.domain.order.service.kafka.OrderEventConsumer;
import com.fream.back.domain.order.service.kafka.OrderEventProducer;
import com.fream.back.domain.payment.dto.CardPaymentRequestDto;
import com.fream.back.domain.payment.dto.GeneralPaymentRequestDto;
import com.fream.back.domain.payment.dto.PaymentRequestDto;
import com.fream.back.domain.payment.entity.Payment;
import com.fream.back.domain.payment.entity.PaymentInfo;
import com.fream.back.domain.payment.entity.PaymentStatus;
import com.fream.back.domain.payment.portone.PortOneApiClient;
import com.fream.back.domain.payment.repository.PaymentRepository;
import com.fream.back.domain.payment.service.command.PaymentCommandService;
import com.fream.back.domain.payment.service.query.PaymentInfoQueryService;
import com.fream.back.domain.sale.service.query.SaleBidQueryService;
import com.fream.back.domain.shipment.entity.OrderShipment;
import com.fream.back.domain.shipment.service.command.OrderShipmentCommandService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.service.query.UserQueryService;
import com.fream.back.domain.warehouseStorage.service.command.WarehouseStorageCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 주문 처리 이벤트 → 실제 결제 명령 서비스 → 주문 상태·배송·알림 결과를 승인/거절/일반 결제별로 확인한다.
 */
class CardDeclineOrderHiddenTest {

    PaymentRepository paymentRepository;
    PaymentInfoQueryService paymentInfoQueryService;
    PortOneApiClient portOne;
    PaymentCommandService paymentCommandService;

    OrderRepository orderRepository;
    OrderShipmentCommandService shipments;
    UserQueryService userQueryService;
    OrderEventProducer producer;
    SimpMessagingTemplate messaging;
    OrderEventConsumer consumer;

    final User buyer = User.builder().id(1L).email("buyer@test.com").build();
    Order order;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentInfoQueryService = mock(PaymentInfoQueryService.class);
        portOne = mock(PortOneApiClient.class);
        paymentCommandService = new PaymentCommandService(paymentRepository, paymentInfoQueryService, portOne,
                mock(NotificationCommandService.class), mock(SaleBidQueryService.class));

        orderRepository = mock(OrderRepository.class);
        shipments = mock(OrderShipmentCommandService.class);
        userQueryService = mock(UserQueryService.class);
        producer = mock(OrderEventProducer.class);
        messaging = mock(SimpMessagingTemplate.class);
        consumer = new OrderEventConsumer(orderRepository, paymentCommandService, shipments,
                mock(WarehouseStorageCommandService.class), userQueryService, producer, messaging,
                (KafkaTemplate<String, Map<String, Object>>) mock(KafkaTemplate.class));

        order = Order.builder().id(100L).user(buyer).totalAmount(30000).status(OrderStatus.PENDING_PAYMENT).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentInfoQueryService.getPaymentInfoEntity(eq("buyer@test.com"), eq(7L)))
                .thenReturn(PaymentInfo.builder().cardNumber("1234567812345678").build());
        when(shipments.createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(OrderShipment.builder().build());
    }

    private CardPaymentRequestDto card() {
        return CardPaymentRequestDto.builder()
                .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build();
    }

    private void handle(PaymentRequestDto pay) {
        PayAndShipmentRequestDto request = PayAndShipmentRequestDto.builder()
                .paymentRequest(pay)
                .receiverName("홍길동").receiverPhone("01011112222").postalCode("06236").address("서울시 강남구")
                .warehouseStorage(false)
                .build();
        consumer.handleOrderProcessingEvent(
                OrderProcessingEvent.create(100L, "buyer@test.com", request), mock(Acknowledgment.class));
    }

    private void verifyNoSuccessNotice() {
        verify(messaging, never()).convertAndSendToUser(anyString(), anyString(),
                argThat((Object m) -> m instanceof Map && "SUCCESS".equals(((Map<?, ?>) m).get("status"))));
    }

    @Test
    void declinedCard_orderStaysPending_noShipment_noSuccessNotice() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));

        handle(card());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        verify(shipments, never()).createOrderShipment(any(), any(), any(), any(), any());
        verifyNoSuccessNotice();
    }

    @Test
    void declinedCardPayment_isNotASuccessfulPayment() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));

        Payment payment = paymentCommandService.processPayment(Order.builder().id(100L).user(buyer).build(), buyer, card());

        assertThat(payment.isSuccess()).isFalse();
        assertThat(payment.getStatus()).isNotEqualTo(PaymentStatus.PAID);
    }

    @Test
    void approvedCard_orderMovesToPreparing_withShipment() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "success", "imp_uid", "imp_ok"));

        handle(card());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
        assertThat(order.getPayment().isSuccess()).isTrue();
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(shipments, times(1)).createOrderShipment(eq(order), eq("홍길동"), eq("01011112222"), eq("06236"), eq("서울시 강남구"));
    }

    @Test
    void generalPayment_orderMovesToPreparing() {
        handle(GeneralPaymentRequestDto.builder().orderId(100L).userEmail("buyer@test.com").paidAmount(30000)
                .impUid("imp_g").pgProvider("kcp").build());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
        assertThat(order.getPayment().isSuccess()).isTrue();
        verify(shipments, times(1)).createOrderShipment(any(), any(), any(), any(), any());
    }
}
