package com.fream.back.hidden.c02;

import com.fream.back.domain.notification.service.command.NotificationCommandService;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderStatus;
import com.fream.back.domain.payment.dto.CardPaymentRequestDto;
import com.fream.back.domain.payment.dto.GeneralPaymentRequestDto;
import com.fream.back.domain.payment.entity.Payment;
import com.fream.back.domain.payment.entity.PaymentInfo;
import com.fream.back.domain.payment.portone.PortOneApiClient;
import com.fream.back.domain.payment.repository.PaymentRepository;
import com.fream.back.domain.payment.service.command.PaymentCommandService;
import com.fream.back.domain.payment.service.query.PaymentInfoQueryService;
import com.fream.back.domain.sale.service.query.SaleBidQueryService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 실제 결제 명령 서비스로 만든 결제를 실제 DB에 저장한 뒤, "결제 완료" 내역 조회 결과를 확인한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class PaidHistoryHiddenTest {

    @Autowired EntityManager em;
    @Autowired PaymentRepository paymentRepository;

    @Test
    void declinedCardIsNotListedAsPaid_butApprovedAndGeneralAre() {
        User buyer = User.builder().email("buyer@test.com").password("pw").referralCode("R1").phoneNumber("010").build();
        em.persist(buyer);
        Order declinedOrder = Order.builder().user(buyer).totalAmount(30000).status(OrderStatus.PENDING_PAYMENT).build();
        Order approvedOrder = Order.builder().user(buyer).totalAmount(40000).status(OrderStatus.PENDING_PAYMENT).build();
        Order generalOrder = Order.builder().user(buyer).totalAmount(50000).status(OrderStatus.PENDING_PAYMENT).build();
        em.persist(declinedOrder);
        em.persist(approvedOrder);
        em.persist(generalOrder);
        em.flush();

        PaymentInfoQueryService infos = mock(PaymentInfoQueryService.class);
        PortOneApiClient portOne = mock(PortOneApiClient.class);
        when(infos.getPaymentInfoEntity(anyString(), any())).thenReturn(PaymentInfo.builder().cardNumber("1111").build());
        PaymentCommandService service = new PaymentCommandService(paymentRepository, infos, portOne,
                mock(NotificationCommandService.class), mock(SaleBidQueryService.class));

        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));
        Payment declined = service.processPayment(declinedOrder, buyer, CardPaymentRequestDto.builder()
                .orderId(declinedOrder.getId()).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build());

        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "success", "imp_uid", "imp_ok"));
        Payment approved = service.processPayment(approvedOrder, buyer, CardPaymentRequestDto.builder()
                .orderId(approvedOrder.getId()).userEmail("buyer@test.com").paidAmount(40000).paymentInfoId(7L).build());

        Payment general = service.processPayment(generalOrder, buyer, GeneralPaymentRequestDto.builder()
                .orderId(generalOrder.getId()).userEmail("buyer@test.com").paidAmount(50000).impUid("imp_g").build());

        em.flush();
        em.clear();

        assertThat(paymentRepository.findByOrder_User_EmailAndIsSuccess("buyer@test.com", true))
                .extracting(Payment::getId).containsExactlyInAnyOrder(approved.getId(), general.getId());
        assertThat(paymentRepository.findByOrder_User_EmailAndIsSuccess("buyer@test.com", false))
                .extracting(Payment::getId).containsExactly(declined.getId());
        assertThat(paymentRepository.existsSuccessfulPaymentByOrderId(declinedOrder.getId())).isFalse();
        assertThat(paymentRepository.existsSuccessfulPaymentByOrderId(approvedOrder.getId())).isTrue();
    }
}
