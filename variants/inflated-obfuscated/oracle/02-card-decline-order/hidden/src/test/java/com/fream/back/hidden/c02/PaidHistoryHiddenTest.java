package com.fream.back.hidden.c02;

import com.fream.back.domain.m52.service.command.Xbp24CommandService;
import com.fream.back.domain.m99.entity.Xhq12;
import com.fream.back.domain.m99.entity.Xhq12Status;
import com.fream.back.domain.m64.dto.Xxf47RequestDto;
import com.fream.back.domain.m64.dto.Xgg74RequestDto;
import com.fream.back.domain.m64.entity.Xjt45;
import com.fream.back.domain.m64.entity.Xjt45Info;
import com.fream.back.domain.m64.p02.Xhs91ApiClient;
import com.fream.back.domain.m64.repository.Xjt45Repository;
import com.fream.back.domain.m64.service.command.Xjt45CommandService;
import com.fream.back.domain.m64.service.query.Xjt45InfoQueryService;
import com.fream.back.domain.m85.service.query.Xzt63QueryService;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.global.config.Xzj77Config;
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
@Import(Xzj77Config.class)
class PaidHistoryHiddenTest {

    @Autowired EntityManager em;
    @Autowired Xjt45Repository paymentRepository;

    @Test
    void declinedCardIsNotListedAsPaid_butApprovedAndGeneralAre() {
        Xjm45 buyer = Xjm45.builder().email("buyer@test.com").password("pw").referralCode("R1").phoneNumber("010").build();
        em.persist(buyer);
        Xhq12 declinedOrder = Xhq12.builder().user(buyer).totalAmount(30000).status(Xhq12Status.PENDING_PAYMENT).build();
        Xhq12 approvedOrder = Xhq12.builder().user(buyer).totalAmount(40000).status(Xhq12Status.PENDING_PAYMENT).build();
        Xhq12 generalOrder = Xhq12.builder().user(buyer).totalAmount(50000).status(Xhq12Status.PENDING_PAYMENT).build();
        em.persist(declinedOrder);
        em.persist(approvedOrder);
        em.persist(generalOrder);
        em.flush();

        Xjt45InfoQueryService infos = mock(Xjt45InfoQueryService.class);
        Xhs91ApiClient portOne = mock(Xhs91ApiClient.class);
        when(infos.getPaymentInfoEntity(anyString(), any())).thenReturn(Xjt45Info.builder().cardNumber("1111").build());
        Xjt45CommandService service = new Xjt45CommandService(paymentRepository, infos, portOne,
                mock(Xbp24CommandService.class), mock(Xzt63QueryService.class));

        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed", "imp_uid", "imp_d"));
        Xjt45 declined = service.processPayment(declinedOrder, buyer, Xxf47RequestDto.builder()
                .orderId(declinedOrder.getId()).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build());

        when(portOne.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "success", "imp_uid", "imp_ok"));
        Xjt45 approved = service.processPayment(approvedOrder, buyer, Xxf47RequestDto.builder()
                .orderId(approvedOrder.getId()).userEmail("buyer@test.com").paidAmount(40000).paymentInfoId(7L).build());

        Xjt45 general = service.processPayment(generalOrder, buyer, Xgg74RequestDto.builder()
                .orderId(generalOrder.getId()).userEmail("buyer@test.com").paidAmount(50000).impUid("imp_g").build());

        em.flush();
        em.clear();

        assertThat(paymentRepository.findByOrder_User_EmailAndIsSuccess("buyer@test.com", true))
                .extracting(Xjt45::getId).containsExactlyInAnyOrder(approved.getId(), general.getId());
        assertThat(paymentRepository.findByOrder_User_EmailAndIsSuccess("buyer@test.com", false))
                .extracting(Xjt45::getId).containsExactly(declined.getId());
        assertThat(paymentRepository.existsSuccessfulPaymentByOrderId(declinedOrder.getId())).isFalse();
        assertThat(paymentRepository.existsSuccessfulPaymentByOrderId(approvedOrder.getId())).isTrue();
    }
}
