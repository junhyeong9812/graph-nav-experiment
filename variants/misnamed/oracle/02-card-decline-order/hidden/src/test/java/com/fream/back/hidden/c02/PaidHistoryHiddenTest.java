package com.fream.back.hidden.c02;

import com.fream.back.domain.payment.service.command.PaymentCommandService;
import com.fream.back.domain.faq.entity.Faq;
import com.fream.back.domain.faq.entity.FaqStatus;
import com.fream.back.domain.notification.dto.CardNotificationRequestDto;
import com.fream.back.domain.notification.dto.GeneralNotificationRequestDto;
import com.fream.back.domain.notification.entity.Notification;
import com.fream.back.domain.notification.entity.NotificationInfo;
import com.fream.back.domain.notification.portone.PortOneApiClient;
import com.fream.back.domain.notification.repository.NotificationRepository;
import com.fream.back.domain.notification.service.command.NotificationCommandService;
import com.fream.back.domain.notification.service.query.NotificationInfoQueryService;
import com.fream.back.domain.style.service.query.StyleBidQueryService;
import com.fream.back.domain.product.entity.Product;
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
    @Autowired NotificationRepository paymentRepository;

    @Test
    void declinedCardIsNotListedAsPaid_butApprovedAndGeneralAre() {
        Product buyer = Product.builder().email("buyer@test.com").password("pw").referralCode("R1").phoneNumber("010").build();
        em.persist(buyer);
        Faq declinedOrder = Faq.builder().user(buyer).totalAmount(30000).status(FaqStatus.PENDING_PAYMENT).build();
        Faq approvedOrder = Faq.builder().user(buyer).totalAmount(40000).status(FaqStatus.PENDING_PAYMENT).build();
        Faq generalOrder = Faq.builder().user(buyer).totalAmount(50000).status(FaqStatus.PENDING_PAYMENT).build();
        em.persist(declinedOrder);
        em.persist(approvedOrder);
        em.persist(generalOrder);
        em.flush();

        NotificationInfoQueryService infos = mock(NotificationInfoQueryService.class);
        PortOneApiClient portOne = mock(PortOneApiClient.class);
        when(infos.getPaymentInfoEntity(anyString(), any())).thenReturn(NotificationInfo.builder().cardNumber("1111").build());
        NotificationCommandService service = new NotificationCommandService(paymentRepository, infos, portOne,
                mock(PaymentCommandService.class), mock(StyleBidQueryService.class));

        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));
        Notification declined = service.processPayment(declinedOrder, buyer, CardNotificationRequestDto.builder()
                .orderId(declinedOrder.getId()).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build());

        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "success", "imp_uid", "imp_ok"));
        Notification approved = service.processPayment(approvedOrder, buyer, CardNotificationRequestDto.builder()
                .orderId(approvedOrder.getId()).userEmail("buyer@test.com").paidAmount(40000).paymentInfoId(7L).build());

        Notification general = service.processPayment(generalOrder, buyer, GeneralNotificationRequestDto.builder()
                .orderId(generalOrder.getId()).userEmail("buyer@test.com").paidAmount(50000).impUid("imp_g").build());

        em.flush();
        em.clear();

        assertThat(paymentRepository.findByOrder_User_EmailAndIsSuccess("buyer@test.com", true))
                .extracting(Notification::getId).containsExactlyInAnyOrder(approved.getId(), general.getId());
        assertThat(paymentRepository.findByOrder_User_EmailAndIsSuccess("buyer@test.com", false))
                .extracting(Notification::getId).containsExactly(declined.getId());
        assertThat(paymentRepository.existsSuccessfulPaymentByOrderId(declinedOrder.getId())).isFalse();
        assertThat(paymentRepository.existsSuccessfulPaymentByOrderId(approvedOrder.getId())).isTrue();
    }
}
