package com.fream.back.hidden.c02;

import com.fream.back.domain.m15.service.command.Xkh73CommandService;
import com.fream.back.domain.m16.dto.Xrt49RequestDto;
import com.fream.back.domain.m16.dto.kafka.Xmr97Event;
import com.fream.back.domain.m16.entity.Xdg64;
import com.fream.back.domain.m16.entity.Xdg64Status;
import com.fream.back.domain.m16.repository.Xdg64Repository;
import com.fream.back.domain.m16.service.kafka.Xdg64EventConsumer;
import com.fream.back.domain.m16.service.kafka.Xdg64EventProducer;
import com.fream.back.domain.m06.dto.Xqg94RequestDto;
import com.fream.back.domain.m06.dto.Xjt86RequestDto;
import com.fream.back.domain.m06.dto.Xbf54RequestDto;
import com.fream.back.domain.m06.entity.Xbf54;
import com.fream.back.domain.m06.entity.Xbf54Info;
import com.fream.back.domain.m06.entity.Xbf54Status;
import com.fream.back.domain.m06.p05.Xxv33ApiClient;
import com.fream.back.domain.m06.repository.Xbf54Repository;
import com.fream.back.domain.m06.service.command.Xbf54CommandService;
import com.fream.back.domain.m06.service.query.Xbf54InfoQueryService;
import com.fream.back.domain.m10.service.query.Xzz58QueryService;
import com.fream.back.domain.m12.entity.Xhx14;
import com.fream.back.domain.m12.service.command.Xhx14CommandService;
import com.fream.back.domain.m13.entity.Xrd81;
import com.fream.back.domain.m13.service.query.Xrd81QueryService;
import com.fream.back.domain.m07.service.command.Xnn94CommandService;
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

    Xbf54Repository paymentRepository;
    Xbf54InfoQueryService paymentInfoQueryService;
    Xxv33ApiClient portOne;
    Xbf54CommandService paymentCommandService;

    Xdg64Repository orderRepository;
    Xhx14CommandService shipments;
    Xrd81QueryService userQueryService;
    Xdg64EventProducer producer;
    SimpMessagingTemplate messaging;
    Xdg64EventConsumer consumer;

    final Xrd81 buyer = Xrd81.builder().id(1L).email("buyer@test.com").build();
    Xdg64 order;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        paymentRepository = mock(Xbf54Repository.class);
        paymentInfoQueryService = mock(Xbf54InfoQueryService.class);
        portOne = mock(Xxv33ApiClient.class);
        paymentCommandService = new Xbf54CommandService(paymentRepository, paymentInfoQueryService, portOne,
                mock(Xkh73CommandService.class), mock(Xzz58QueryService.class));

        orderRepository = mock(Xdg64Repository.class);
        shipments = mock(Xhx14CommandService.class);
        userQueryService = mock(Xrd81QueryService.class);
        producer = mock(Xdg64EventProducer.class);
        messaging = mock(SimpMessagingTemplate.class);
        consumer = new Xdg64EventConsumer(orderRepository, paymentCommandService, shipments,
                mock(Xnn94CommandService.class), userQueryService, producer, messaging,
                (KafkaTemplate<String, Map<String, Object>>) mock(KafkaTemplate.class));

        order = Xdg64.builder().id(100L).user(buyer).totalAmount(30000).status(Xdg64Status.PENDING_PAYMENT).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentInfoQueryService.getPaymentInfoEntity(eq("buyer@test.com"), eq(7L)))
                .thenReturn(Xbf54Info.builder().cardNumber("1234567812345678").build());
        when(shipments.createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(Xhx14.builder().build());
    }

    private Xqg94RequestDto card() {
        return Xqg94RequestDto.builder()
                .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build();
    }

    private void handle(Xbf54RequestDto pay) {
        Xrt49RequestDto request = Xrt49RequestDto.builder()
                .paymentRequest(pay)
                .receiverName("홍길동").receiverPhone("01011112222").postalCode("06236").address("서울시 강남구")
                .warehouseStorage(false)
                .build();
        consumer.handleOrderProcessingEvent(
                Xmr97Event.create(100L, "buyer@test.com", request), mock(Acknowledgment.class));
    }

    private void verifyNoSuccessNotice() {
        verify(messaging, never()).convertAndSendToUser(anyString(), anyString(),
                argThat((Object m) -> m instanceof Map && "SUCCESS".equals(((Map<?, ?>) m).get("status"))));
    }

    @Test
    void declinedCard_orderStaysPending_noShipment_noSuccessNotice() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));

        handle(card());

        assertThat(order.getStatus()).isEqualTo(Xdg64Status.PENDING_PAYMENT);
        verify(shipments, never()).createOrderShipment(any(), any(), any(), any(), any());
        verifyNoSuccessNotice();
    }

    @Test
    void declinedCardPayment_isNotASuccessfulPayment() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));

        Xbf54 payment = paymentCommandService.processPayment(Xdg64.builder().id(100L).user(buyer).build(), buyer, card());

        assertThat(payment.isSuccess()).isFalse();
        assertThat(payment.getStatus()).isNotEqualTo(Xbf54Status.PAID);
    }

    @Test
    void approvedCard_orderMovesToPreparing_withShipment() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "success", "imp_uid", "imp_ok"));

        handle(card());

        assertThat(order.getStatus()).isEqualTo(Xdg64Status.PREPARING);
        assertThat(order.getPayment().isSuccess()).isTrue();
        assertThat(order.getPayment().getStatus()).isEqualTo(Xbf54Status.PAID);
        verify(shipments, times(1)).createOrderShipment(eq(order), eq("홍길동"), eq("01011112222"), eq("06236"), eq("서울시 강남구"));
    }

    @Test
    void generalPayment_orderMovesToPreparing() {
        handle(Xjt86RequestDto.builder().orderId(100L).userEmail("buyer@test.com").paidAmount(30000)
                .impUid("imp_g").pgProvider("kcp").build());

        assertThat(order.getStatus()).isEqualTo(Xdg64Status.PREPARING);
        assertThat(order.getPayment().isSuccess()).isTrue();
        verify(shipments, times(1)).createOrderShipment(any(), any(), any(), any(), any());
    }
}
