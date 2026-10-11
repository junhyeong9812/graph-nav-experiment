package com.fream.back.hidden.c04;

import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.repository.OrderBidRepository;
import com.fream.back.domain.order.service.command.OrderBidCommandService;
import com.fream.back.domain.order.service.command.OrderCommandService;
import com.fream.back.domain.payment.dto.GeneralPaymentRequestDto;
import com.fream.back.domain.payment.dto.PaymentRequestDto;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.product.service.productSize.ProductSizeQueryService;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleBid;
import com.fream.back.domain.sale.service.query.SaleBidQueryService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.service.query.UserQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 지점 1: 일반 구매 입찰 등록. */
@ExtendWith(MockitoExtension.class)
class PurchaseBidRegistrationHiddenTest {

    @Mock OrderBidRepository orderBidRepository;
    @Mock OrderCommandService orderCommandService;
    @Mock ProductSizeQueryService productSizeQueryService;
    @Mock UserQueryService userQueryService;
    @Mock SaleBidQueryService saleBidQueryService;

    @InjectMocks OrderBidCommandService service;

    private final User buyer = User.builder().id(1L).email("buyer@test.com").build();
    private final ProductSize size = ProductSize.builder().id(10L).size("260").build();
    private final Order order = Order.builder().id(100L).user(buyer).build();

    private OrderBid register() {
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(productSizeQueryService.findById(10L)).thenReturn(Optional.of(size));
        when(orderCommandService.createOrderFromBid(buyer, size, 180000)).thenReturn(order);
        when(orderBidRepository.save(any(OrderBid.class))).thenAnswer(inv -> inv.getArgument(0));
        return service.createOrderBid("buyer@test.com", 10L, 180000);
    }

    @Test
    void registeredBidIsPendingAndKeepsItsOrder() {
        OrderBid bid = register();

        assertThat(bid.getStatus()).isEqualTo(BidStatus.PENDING);
        assertThat(bid.getOrder()).isSameAs(order);
        assertThat(bid.getUser()).isSameAs(buyer);
        assertThat(bid.getBidPrice()).isEqualTo(180000);
        assertThat(bid.getSale()).isNull();
        assertThat(bid.isInstantPurchase()).isFalse();
    }

    @Test
    void registeredBidCanBeCancelledTogetherWithItsOrder() {
        OrderBid bid = register();
        when(orderBidRepository.findById(55L)).thenReturn(Optional.of(bid));

        assertThatCode(() -> service.deleteOrderBid(55L)).doesNotThrowAnyException();
        verify(orderCommandService).deleteOrder(100L);
        verify(orderBidRepository).delete(bid);
    }

    @Test
    void registeredBidCanLaterBeMatched() {
        OrderBid bid = register();

        assertThatCode(() -> service.matchOrderBid(bid)).doesNotThrowAnyException();
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
    }

    @Test
    void instantPurchaseIsStillMatchedRightAway() {
        User seller = User.builder().id(2L).build();
        Sale sale = Sale.builder().id(50L).seller(seller).build();
        SaleBid saleBid = SaleBid.builder().id(70L).bidPrice(210000).productSize(size).sale(sale).build();
        Order instantOrder = Order.builder().id(101L).user(buyer).build();
        PaymentRequestDto pay = GeneralPaymentRequestDto.builder().impUid("imp").build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(saleBidQueryService.findById(70L)).thenReturn(saleBid);
        when(orderCommandService.createInstantOrder(buyer, saleBid, 3L, true, pay)).thenReturn(instantOrder);
        when(orderBidRepository.save(any(OrderBid.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderBid bid = service.createInstantOrderBid("buyer@test.com", 70L, 3L, true, pay);

        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
        assertThat(bid.isInstantPurchase()).isTrue();
        assertThat(bid.getOrder()).isSameAs(instantOrder);
        assertThat(bid.getSale()).isSameAs(sale);
    }
}
