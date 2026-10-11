package com.fream.back.hidden.c02;

import com.fream.back.domain.payment.service.command.PaymentCommandService;
import com.fream.back.domain.faq.dto.PayAndAddressRequestDto;
import com.fream.back.domain.faq.dto.kafka.FaqProcessingEvent;
import com.fream.back.domain.faq.entity.Faq;
import com.fream.back.domain.faq.entity.FaqStatus;
import com.fream.back.domain.faq.repository.FaqRepository;
import com.fream.back.domain.faq.service.kafka.FaqEventConsumer;
import com.fream.back.domain.faq.service.kafka.FaqEventProducer;
import com.fream.back.domain.notification.dto.CardNotificationRequestDto;
import com.fream.back.domain.notification.dto.GeneralNotificationRequestDto;
import com.fream.back.domain.notification.dto.NotificationRequestDto;
import com.fream.back.domain.notification.entity.Notification;
import com.fream.back.domain.notification.entity.NotificationInfo;
import com.fream.back.domain.notification.entity.NotificationStatus;
import com.fream.back.domain.notification.portone.PortOneApiClient;
import com.fream.back.domain.notification.repository.NotificationRepository;
import com.fream.back.domain.notification.service.command.NotificationCommandService;
import com.fream.back.domain.notification.service.query.NotificationInfoQueryService;
import com.fream.back.domain.style.service.query.StyleBidQueryService;
import com.fream.back.domain.address.entity.FaqAddress;
import com.fream.back.domain.address.service.command.FaqAddressCommandService;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.service.query.ProductQueryService;
import com.fream.back.domain.notice.service.command.NoticeCommandService;
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

    NotificationRepository paymentRepository;
    NotificationInfoQueryService paymentInfoQueryService;
    PortOneApiClient portOne;
    NotificationCommandService paymentCommandService;

    FaqRepository orderRepository;
    FaqAddressCommandService shipments;
    ProductQueryService userQueryService;
    FaqEventProducer producer;
    SimpMessagingTemplate messaging;
    FaqEventConsumer consumer;

    final Product buyer = Product.builder().id(1L).email("buyer@test.com").build();
    Faq order;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        paymentRepository = mock(NotificationRepository.class);
        paymentInfoQueryService = mock(NotificationInfoQueryService.class);
        portOne = mock(PortOneApiClient.class);
        paymentCommandService = new NotificationCommandService(paymentRepository, paymentInfoQueryService, portOne,
                mock(PaymentCommandService.class), mock(StyleBidQueryService.class));

        orderRepository = mock(FaqRepository.class);
        shipments = mock(FaqAddressCommandService.class);
        userQueryService = mock(ProductQueryService.class);
        producer = mock(FaqEventProducer.class);
        messaging = mock(SimpMessagingTemplate.class);
        consumer = new FaqEventConsumer(orderRepository, paymentCommandService, shipments,
                mock(NoticeCommandService.class), userQueryService, producer, messaging,
                (KafkaTemplate<String, Map<String, Object>>) mock(KafkaTemplate.class));

        order = Faq.builder().id(100L).user(buyer).totalAmount(30000).status(FaqStatus.PENDING_PAYMENT).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentInfoQueryService.getPaymentInfoEntity(eq("buyer@test.com"), eq(7L)))
                .thenReturn(NotificationInfo.builder().cardNumber("1234567812345678").build());
        when(shipments.createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(FaqAddress.builder().build());
    }

    private CardNotificationRequestDto card() {
        return CardNotificationRequestDto.builder()
                .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build();
    }

    private void handle(NotificationRequestDto pay) {
        PayAndAddressRequestDto request = PayAndAddressRequestDto.builder()
                .paymentRequest(pay)
                .receiverName("홍길동").receiverPhone("01011112222").postalCode("06236").address("서울시 강남구")
                .warehouseStorage(false)
                .build();
        consumer.handleOrderProcessingEvent(
                FaqProcessingEvent.create(100L, "buyer@test.com", request), mock(Acknowledgment.class));
    }

    private void verifyNoSuccessNotice() {
        verify(messaging, never()).convertAndSendToUser(anyString(), anyString(),
                argThat((Object m) -> m instanceof Map && "SUCCESS".equals(((Map<?, ?>) m).get("status"))));
    }

    @Test
    void declinedCard_orderStaysPending_noShipment_noSuccessNotice() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));

        handle(card());

        assertThat(order.getStatus()).isEqualTo(FaqStatus.PENDING_PAYMENT);
        verify(shipments, never()).createOrderShipment(any(), any(), any(), any(), any());
        verifyNoSuccessNotice();
    }

    @Test
    void declinedCardPayment_isNotASuccessfulPayment() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));

        Notification payment = paymentCommandService.processPayment(Faq.builder().id(100L).user(buyer).build(), buyer, card());

        assertThat(payment.isSuccess()).isFalse();
        assertThat(payment.getStatus()).isNotEqualTo(NotificationStatus.PAID);
    }

    @Test
    void approvedCard_orderMovesToPreparing_withShipment() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "success", "imp_uid", "imp_ok"));

        handle(card());

        assertThat(order.getStatus()).isEqualTo(FaqStatus.PREPARING);
        assertThat(order.getPayment().isSuccess()).isTrue();
        assertThat(order.getPayment().getStatus()).isEqualTo(NotificationStatus.PAID);
        verify(shipments, times(1)).createOrderShipment(eq(order), eq("홍길동"), eq("01011112222"), eq("06236"), eq("서울시 강남구"));
    }

    @Test
    void generalPayment_orderMovesToPreparing() {
        handle(GeneralNotificationRequestDto.builder().orderId(100L).userEmail("buyer@test.com").paidAmount(30000)
                .impUid("imp_g").pgProvider("kcp").build());

        assertThat(order.getStatus()).isEqualTo(FaqStatus.PREPARING);
        assertThat(order.getPayment().isSuccess()).isTrue();
        verify(shipments, times(1)).createOrderShipment(any(), any(), any(), any(), any());
    }
}
