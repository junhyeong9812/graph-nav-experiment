package com.fream.back.visible.c04;

import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.repository.OrderBidRepository;
import com.fream.back.domain.order.service.command.OrderBidCommandService;
import com.fream.back.domain.order.service.command.OrderCommandService;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.product.service.productSize.ProductSizeQueryService;
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

/**
 * 일반 구매 입찰 등록 직후의 상태와 취소 가능 여부.
 */
@ExtendWith(MockitoExtension.class)
class PurchaseBidRegistrationTest {

    @Mock OrderBidRepository orderBidRepository;
    @Mock OrderCommandService orderCommandService;
    @Mock ProductSizeQueryService productSizeQueryService;
    @Mock UserQueryService userQueryService;
    @Mock SaleBidQueryService saleBidQueryService;

    @InjectMocks OrderBidCommandService service;

    private final User buyer = User.builder().id(1L).email("buyer@test.com").build();
    private final ProductSize size = ProductSize.builder().id(10L).size("260").build();

    private OrderBid register() {
        Order order = Order.builder().id(100L).user(buyer).build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(productSizeQueryService.findById(10L)).thenReturn(Optional.of(size));
        when(orderCommandService.createOrderFromBid(buyer, size, 180000)).thenReturn(order);
        when(orderBidRepository.save(any(OrderBid.class))).thenAnswer(inv -> inv.getArgument(0));
        return service.createOrderBid("buyer@test.com", 10L, 180000);
    }

    @Test
    void newPurchaseBidIsWaitingForASeller() {
        OrderBid bid = register();

        assertThat(bid.getStatus()).isEqualTo(BidStatus.PENDING);
        assertThat(bid.getSale()).isNull();
        assertThat(bid.isInstantPurchase()).isFalse();
    }

    @Test
    void newPurchaseBidCanBeCancelled() {
        OrderBid bid = register();
        when(orderBidRepository.findById(55L)).thenReturn(Optional.of(bid));

        assertThatCode(() -> service.deleteOrderBid(55L)).doesNotThrowAnyException();
        verify(orderBidRepository).delete(bid);
    }
}
