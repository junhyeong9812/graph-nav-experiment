package com.fream.back.hidden.c02;

import com.fream.back.domain.m15.service.command.Xkh73CommandService;
import com.fream.back.domain.m16.entity.Xdg64;
import com.fream.back.domain.m16.entity.Xdg64Status;
import com.fream.back.domain.m06.dto.Xqg94RequestDto;
import com.fream.back.domain.m06.dto.Xjt86RequestDto;
import com.fream.back.domain.m06.entity.Xbf54;
import com.fream.back.domain.m06.entity.Xbf54Info;
import com.fream.back.domain.m06.p05.Xxv33ApiClient;
import com.fream.back.domain.m06.repository.Xbf54Repository;
import com.fream.back.domain.m06.service.command.Xbf54CommandService;
import com.fream.back.domain.m06.service.query.Xbf54InfoQueryService;
import com.fream.back.domain.m10.service.query.Xzz58QueryService;
import com.fream.back.domain.m13.entity.Xrd81;
import com.fream.back.global.config.Xsd33Config;
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
@Import(Xsd33Config.class)
class PaidHistoryHiddenTest {

    @Autowired EntityManager em;
    @Autowired Xbf54Repository paymentRepository;

    @Test
    void declinedCardIsNotListedAsPaid_butApprovedAndGeneralAre() {
        Xrd81 buyer = Xrd81.builder().email("buyer@test.com").password("pw").referralCode("R1").phoneNumber("010").build();
        em.persist(buyer);
        Xdg64 declinedOrder = Xdg64.builder().user(buyer).totalAmount(30000).status(Xdg64Status.PENDING_PAYMENT).build();
        Xdg64 approvedOrder = Xdg64.builder().user(buyer).totalAmount(40000).status(Xdg64Status.PENDING_PAYMENT).build();
        Xdg64 generalOrder = Xdg64.builder().user(buyer).totalAmount(50000).status(Xdg64Status.PENDING_PAYMENT).build();
        em.persist(declinedOrder);
        em.persist(approvedOrder);
        em.persist(generalOrder);
        em.flush();

        Xbf54InfoQueryService infos = mock(Xbf54InfoQueryService.class);
        Xxv33ApiClient portOne = mock(Xxv33ApiClient.class);
        when(infos.getPaymentInfoEntity(anyString(), any())).thenReturn(Xbf54Info.builder().cardNumber("1111").build());
        Xbf54CommandService service = new Xbf54CommandService(paymentRepository, infos, portOne,
                mock(Xkh73CommandService.class), mock(Xzz58QueryService.class));

        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));
        Xbf54 declined = service.processPayment(declinedOrder, buyer, Xqg94RequestDto.builder()
                .orderId(declinedOrder.getId()).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build());

        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "success", "imp_uid", "imp_ok"));
        Xbf54 approved = service.processPayment(approvedOrder, buyer, Xqg94RequestDto.builder()
                .orderId(approvedOrder.getId()).userEmail("buyer@test.com").paidAmount(40000).paymentInfoId(7L).build());

        Xbf54 general = service.processPayment(generalOrder, buyer, Xjt86RequestDto.builder()
                .orderId(generalOrder.getId()).userEmail("buyer@test.com").paidAmount(50000).impUid("imp_g").build());

        em.flush();
        em.clear();

        assertThat(paymentRepository.findByOrder_User_EmailAndIsSuccess("buyer@test.com", true))
                .extracting(Xbf54::getId).containsExactlyInAnyOrder(approved.getId(), general.getId());
        assertThat(paymentRepository.findByOrder_User_EmailAndIsSuccess("buyer@test.com", false))
                .extracting(Xbf54::getId).containsExactly(declined.getId());
        assertThat(paymentRepository.existsSuccessfulPaymentByOrderId(declinedOrder.getId())).isFalse();
        assertThat(paymentRepository.existsSuccessfulPaymentByOrderId(approvedOrder.getId())).isTrue();
    }
}
