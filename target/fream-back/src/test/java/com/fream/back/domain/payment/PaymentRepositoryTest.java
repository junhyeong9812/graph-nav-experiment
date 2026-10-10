package com.fream.back.domain.payment;

import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.entity.OrderStatus;
import com.fream.back.domain.payment.entity.*;
import com.fream.back.domain.payment.repository.PaymentInfoRepository;
import com.fream.back.domain.payment.repository.PaymentRepository;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleStatus;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 결제·결제수단 리포지토리 JPQL(구매자/판매자 경로, impUid, 멱등성 조회, 상태 필터)의 결과를 고정한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class PaymentRepositoryTest {

    @Autowired private EntityManager em;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PaymentInfoRepository paymentInfoRepository;

    private User buyer;
    private User otherBuyer;
    private User seller;

    @BeforeEach
    void setUp() {
        buyer = persistUser("buyer@test.com", "R1");
        otherBuyer = persistUser("other@test.com", "R2");
        seller = persistUser("seller@test.com", "R3");
        em.flush();
    }

    private User persistUser(String email, String ref) {
        User u = User.builder().email(email).password("pw").referralCode(ref).phoneNumber("010").build();
        em.persist(u);
        return u;
    }

    private Order persistOrder(User user) {
        Order order = Order.builder().user(user).totalAmount(1000).status(OrderStatus.PENDING_PAYMENT).build();
        em.persist(order);
        return order;
    }

    private Payment persistGeneral(Order order, User user, double amount, String impUid, PaymentStatus status) {
        GeneralPayment p = GeneralPayment.builder().impUid(impUid).paidAmount(amount).build();
        p.assignOrder(order);
        p.assignUser(user);
        if (status == PaymentStatus.PAID) {
            p.updateStatus(PaymentStatus.PAID);
        }
        em.persist(p);
        return p;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    void findByBuyerEmailAndSuccessFiltersByOrderOwnerAndFlag() {
        Payment ok = persistGeneral(persistOrder(buyer), buyer, 1000, "g1", PaymentStatus.PAID);
        Payment zero = persistGeneral(persistOrder(buyer), buyer, 0, "g2", PaymentStatus.PENDING);
        persistGeneral(persistOrder(otherBuyer), otherBuyer, 1000, "g3", PaymentStatus.PAID);
        flushAndClear();

        assertThat(paymentRepository.findByOrder_User_EmailAndIsSuccess("buyer@test.com", true))
                .extracting(Payment::getId).containsExactly(ok.getId());
        assertThat(paymentRepository.findByOrder_User_EmailAndIsSuccess("buyer@test.com", false))
                .extracting(Payment::getId).containsExactly(zero.getId());
    }

    @Test
    void findBySellerEmailFollowsOrderBidToSale() {
        Sale sale = Sale.builder().seller(seller).status(SaleStatus.SOLD).build();
        em.persist(sale);
        Order soldOrder = persistOrder(buyer);
        em.persist(OrderBid.builder().user(buyer).order(soldOrder).sale(sale).bidPrice(1000)
                .status(BidStatus.MATCHED).build());
        Payment viaBid = persistGeneral(soldOrder, buyer, 1000, "s1", PaymentStatus.PAID);
        // 주문 입찰이 없는 주문의 결제는 판매자 조회에 잡히지 않는다
        persistGeneral(persistOrder(buyer), buyer, 1000, "s2", PaymentStatus.PAID);
        flushAndClear();

        List<Payment> result = paymentRepository.findBySale_Seller_EmailAndIsSuccess("seller@test.com", true);

        assertThat(result).extracting(Payment::getId).containsExactly(viaBid.getId());
        assertThat(paymentRepository.findBySale_Seller_EmailAndIsSuccess("buyer@test.com", true)).isEmpty();
    }

    @Test
    void findByImpUidReturnsOnlyCardPayments() {
        CardPayment card = CardPayment.builder().impUid("imp_card").paidAmount(1000).cardNumber("1111").build();
        card.assignOrder(persistOrder(buyer));
        card.assignUser(buyer);
        em.persist(card);
        persistGeneral(persistOrder(buyer), buyer, 1000, "imp_general", PaymentStatus.PAID);
        flushAndClear();

        assertThat(paymentRepository.findByImpUid("imp_card")).get()
                .extracting(CardPayment::getCardNumber).isEqualTo("1111");
        assertThat(paymentRepository.findByImpUid("imp_general")).isEmpty();
    }

    @Test
    void idempotencyQueriesByOrderId() {
        Order paid = persistOrder(buyer);
        Order pending = persistOrder(buyer);
        Payment paidPayment = persistGeneral(paid, buyer, 1000, "i1", PaymentStatus.PAID);
        Payment failedFlag = persistGeneral(pending, buyer, 0, "i2", PaymentStatus.PENDING);
        flushAndClear();

        assertThat(paymentRepository.findByOrder_Id(paid.getId())).get()
                .extracting(Payment::getId).isEqualTo(paidPayment.getId());
        assertThat(paymentRepository.findByOrder_Id(-1L)).isEmpty();

        assertThat(paymentRepository.existsSuccessfulPaymentByOrderId(paid.getId())).isTrue();
        assertThat(paymentRepository.existsSuccessfulPaymentByOrderId(pending.getId())).isFalse();

        assertThat(paymentRepository.findByOrder_IdAndStatus(paid.getId(), PaymentStatus.PAID))
                .extracting(Payment::getId).containsExactly(paidPayment.getId());
        assertThat(paymentRepository.findByOrder_IdAndStatus(paid.getId(), PaymentStatus.PENDING)).isEmpty();

        assertThat(paymentRepository.findByOrder_IdAndStatus(pending.getId(), PaymentStatus.PENDING))
                .extracting(Payment::getId).containsExactly(failedFlag.getId());
    }

    @Test
    void findPaymentsCreatedSinceUsesCreatedDate() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Payment p = persistGeneral(persistOrder(buyer), buyer, 1000, "c1", PaymentStatus.PAID);
        flushAndClear();

        assertThat(paymentRepository.findPaymentsCreatedSince(before))
                .extracting(Payment::getId).containsExactly(p.getId());
        assertThat(paymentRepository.findPaymentsCreatedSince(LocalDateTime.now().plusDays(1))).isEmpty();
    }

    @Test
    void paymentInfoQueriesAreScopedByUserEmail() {
        PaymentInfo mine = PaymentInfo.builder().user(buyer).cardNumber("1").cardPassword("12")
                .expirationDate("12/30").birthDate("900101").build();
        PaymentInfo theirs = PaymentInfo.builder().user(otherBuyer).cardNumber("2").build();
        em.persist(mine);
        em.persist(theirs);
        flushAndClear();

        assertThat(paymentInfoRepository.findAllByUser_Email("buyer@test.com"))
                .extracting(PaymentInfo::getCardNumber).containsExactly("1");
        assertThat(paymentInfoRepository.findByIdAndUser_Email(mine.getId(), "buyer@test.com")).isPresent();
        assertThat(paymentInfoRepository.findByIdAndUser_Email(theirs.getId(), "buyer@test.com")).isEmpty();
    }
}
