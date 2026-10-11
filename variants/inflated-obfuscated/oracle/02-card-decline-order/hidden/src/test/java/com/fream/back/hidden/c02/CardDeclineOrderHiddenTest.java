package com.fream.back.hidden.c02;

import com.fream.back.domain.m52.service.command.Xbp24CommandService;
import com.fream.back.domain.m99.dto.Xsw86RequestDto;
import com.fream.back.domain.m99.dto.kafka.Xrb42Event;
import com.fream.back.domain.m99.entity.Xhq12;
import com.fream.back.domain.m99.entity.Xhq12Status;
import com.fream.back.domain.m99.repository.Xhq12Repository;
import com.fream.back.domain.m99.service.kafka.Xhq12EventConsumer;
import com.fream.back.domain.m99.service.kafka.Xhq12EventProducer;
import com.fream.back.domain.m64.dto.Xxf47RequestDto;
import com.fream.back.domain.m64.dto.Xgg74RequestDto;
import com.fream.back.domain.m64.dto.Xjt45RequestDto;
import com.fream.back.domain.m64.entity.Xjt45;
import com.fream.back.domain.m64.entity.Xjt45Info;
import com.fream.back.domain.m64.entity.Xjt45Status;
import com.fream.back.domain.m64.p02.Xhs91ApiClient;
import com.fream.back.domain.m64.repository.Xjt45Repository;
import com.fream.back.domain.m64.service.command.Xjt45CommandService;
import com.fream.back.domain.m64.service.query.Xjt45InfoQueryService;
import com.fream.back.domain.m85.service.query.Xzt63QueryService;
import com.fream.back.domain.m45.entity.Xfr60;
import com.fream.back.domain.m45.service.command.Xfr60CommandService;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.domain.m22.service.query.Xjm45QueryService;
import com.fream.back.domain.m63.service.command.Xms75CommandService;
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

    Xjt45Repository paymentRepository;
    Xjt45InfoQueryService paymentInfoQueryService;
    Xhs91ApiClient portOne;
    Xjt45CommandService paymentCommandService;

    Xhq12Repository orderRepository;
    Xfr60CommandService shipments;
    Xjm45QueryService userQueryService;
    Xhq12EventProducer producer;
    SimpMessagingTemplate messaging;
    Xhq12EventConsumer consumer;

    final Xjm45 buyer = Xjm45.builder().id(1L).email("buyer@test.com").build();
    Xhq12 order;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        paymentRepository = mock(Xjt45Repository.class);
        paymentInfoQueryService = mock(Xjt45InfoQueryService.class);
        portOne = mock(Xhs91ApiClient.class);
        paymentCommandService = new Xjt45CommandService(paymentRepository, paymentInfoQueryService, portOne,
                mock(Xbp24CommandService.class), mock(Xzt63QueryService.class));

        orderRepository = mock(Xhq12Repository.class);
        shipments = mock(Xfr60CommandService.class);
        userQueryService = mock(Xjm45QueryService.class);
        producer = mock(Xhq12EventProducer.class);
        messaging = mock(SimpMessagingTemplate.class);
        consumer = new Xhq12EventConsumer(orderRepository, paymentCommandService, shipments,
                mock(Xms75CommandService.class), userQueryService, producer, messaging,
                (KafkaTemplate<String, Map<String, Object>>) mock(KafkaTemplate.class));

        order = Xhq12.builder().id(100L).user(buyer).totalAmount(30000).status(Xhq12Status.PENDING_PAYMENT).build();
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentInfoQueryService.getPaymentInfoEntity(eq("buyer@test.com"), eq(7L)))
                .thenReturn(Xjt45Info.builder().cardNumber("1234567812345678").build());
        when(shipments.createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(Xfr60.builder().build());
    }

    private Xxf47RequestDto card() {
        return Xxf47RequestDto.builder()
                .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build();
    }

    private void handle(Xjt45RequestDto pay) {
        Xsw86RequestDto request = Xsw86RequestDto.builder()
                .paymentRequest(pay)
                .receiverName("홍길동").receiverPhone("01011112222").postalCode("06236").address("서울시 강남구")
                .warehouseStorage(false)
                .build();
        consumer.handleOrderProcessingEvent(
                Xrb42Event.create(100L, "buyer@test.com", request), mock(Acknowledgment.class));
    }

    private void verifyNoSuccessNotice() {
        verify(messaging, never()).convertAndSendToUser(anyString(), anyString(),
                argThat((Object m) -> m instanceof Map && "SUCCESS".equals(((Map<?, ?>) m).get("status"))));
    }

    @Test
    void declinedCard_orderStaysPending_noShipment_noSuccessNotice() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));

        handle(card());

        assertThat(order.getStatus()).isEqualTo(Xhq12Status.PENDING_PAYMENT);
        verify(shipments, never()).createOrderShipment(any(), any(), any(), any(), any());
        verifyNoSuccessNotice();
    }

    @Test
    void declinedCardPayment_isNotASuccessfulPayment() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));

        Xjt45 payment = paymentCommandService.processPayment(Xhq12.builder().id(100L).user(buyer).build(), buyer, card());

        assertThat(payment.isSuccess()).isFalse();
        assertThat(payment.getStatus()).isNotEqualTo(Xjt45Status.PAID);
    }

    @Test
    void approvedCard_orderMovesToPreparing_withShipment() {
        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "success", "imp_uid", "imp_ok"));

        handle(card());

        assertThat(order.getStatus()).isEqualTo(Xhq12Status.PREPARING);
        assertThat(order.getPayment().isSuccess()).isTrue();
        assertThat(order.getPayment().getStatus()).isEqualTo(Xjt45Status.PAID);
        verify(shipments, times(1)).createOrderShipment(eq(order), eq("홍길동"), eq("01011112222"), eq("06236"), eq("서울시 강남구"));
    }

    @Test
    void generalPayment_orderMovesToPreparing() {
        handle(Xgg74RequestDto.builder().orderId(100L).userEmail("buyer@test.com").paidAmount(30000)
                .impUid("imp_g").pgProvider("kcp").build());

        assertThat(order.getStatus()).isEqualTo(Xhq12Status.PREPARING);
        assertThat(order.getPayment().isSuccess()).isTrue();
        verify(shipments, times(1)).createOrderShipment(any(), any(), any(), any(), any());
    }
}
