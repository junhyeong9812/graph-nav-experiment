package com.fream.back.domain.order;

import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.exception.*;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 주문 입찰 명령 서비스의 생성 시 상태·즉시구매 플래그·삭제/매칭 가드를 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class OrderBidCommandServiceTest {

    @Mock OrderBidRepository orderBidRepository;
    @Mock OrderCommandService orderCommandService;
    @Mock ProductSizeQueryService productSizeQueryService;
    @Mock UserQueryService userQueryService;
    @Mock SaleBidQueryService saleBidQueryService;

    @InjectMocks OrderBidCommandService service;

    private final User buyer = User.builder().id(1L).email("buyer@test.com").build();

    @Test
    void createOrderBidLinksOrderAndIsSavedAsMatched() {
        ProductSize size = ProductSize.builder().id(10L).build();
        Order order = Order.builder().id(100L).user(buyer).build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(productSizeQueryService.findById(10L)).thenReturn(Optional.of(size));
        when(orderCommandService.createOrderFromBid(buyer, size, 180000)).thenReturn(order);
        when(orderBidRepository.save(any(OrderBid.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderBid bid = service.createOrderBid("buyer@test.com", 10L, 180000);

        assertThat(bid.getUser()).isSameAs(buyer);
        assertThat(bid.getProductSize()).isSameAs(size);
        assertThat(bid.getBidPrice()).isEqualTo(180000);
        assertThat(bid.getOrder()).isSameAs(order);
        assertThat(order.getOrderBid()).isSameAs(bid);
        // 빌더에서 PENDING을 넣지만 assignOrder가 MATCHED로 바꾼다
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
        assertThat(bid.isInstantPurchase()).isFalse();
        assertThat(bid.getSale()).isNull();
    }

    @Test
    void createOrderBidValidationAndMissingSize() {
        assertThatThrownBy(() -> service.createOrderBid(" ", 10L, 1000))
                .isInstanceOf(OrderBidAccessDeniedException.class);
        assertThatThrownBy(() -> service.createOrderBid("buyer@test.com", null, 1000))
                .isInstanceOf(ProductSizeNotFoundException.class);
        assertThatThrownBy(() -> service.createOrderBid("buyer@test.com", 10L, -1))
                .isInstanceOf(InvalidBidPriceException.class);
        verifyNoInteractions(userQueryService);

        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(productSizeQueryService.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createOrderBid("buyer@test.com", 99L, 1000))
                .isInstanceOf(ProductSizeNotFoundException.class)
                .hasMessageContaining("ID: 99");
        verifyNoInteractions(orderCommandService, orderBidRepository);
    }

    @Test
    void createOrderBidWrapsNonOrderExceptionAsCreationFailed() {
        when(userQueryService.findByEmail("buyer@test.com")).thenThrow(new IllegalStateException("no user"));

        assertThatThrownBy(() -> service.createOrderBid("buyer@test.com", 10L, 1000))
                .isInstanceOf(OrderBidCreationFailedException.class)
                .hasMessageContaining("주문 입찰 생성")
                .hasMessageContaining("no user");
    }

    @Test
    void createInstantOrderBidCopiesSaleBidAndMarksInstantPurchase() {
        User seller = User.builder().id(2L).build();
        ProductSize size = ProductSize.builder().id(10L).build();
        Sale sale = Sale.builder().id(50L).seller(seller).build();
        SaleBid saleBid = SaleBid.builder().id(70L).bidPrice(210000).productSize(size).sale(sale).build();
        Order order = Order.builder().id(100L).user(buyer).build();
        PaymentRequestDto pay = GeneralPaymentRequestDto.builder().impUid("imp").build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(saleBidQueryService.findById(70L)).thenReturn(saleBid);
        when(orderCommandService.createInstantOrder(buyer, saleBid, 3L, true, pay)).thenReturn(order);
        when(orderBidRepository.save(any(OrderBid.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderBid bid = service.createInstantOrderBid("buyer@test.com", 70L, 3L, true, pay);

        assertThat(bid.getUser()).isSameAs(buyer);
        assertThat(bid.getBidPrice()).isEqualTo(210000);
        assertThat(bid.getProductSize()).isSameAs(size);
        assertThat(bid.getSale()).isSameAs(sale);
        assertThat(bid.getOrder()).isSameAs(order);
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
        assertThat(bid.isInstantPurchase()).isTrue();
    }

    @Test
    void createInstantOrderBidRejectsMissingSaleBidOrSale() {
        PaymentRequestDto pay = GeneralPaymentRequestDto.builder().build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(saleBidQueryService.findById(70L)).thenReturn(null);
        when(saleBidQueryService.findById(71L)).thenReturn(SaleBid.builder().id(71L).build());

        assertThatThrownBy(() -> service.createInstantOrderBid("buyer@test.com", 70L, 3L, false, pay))
                .isInstanceOf(SaleBidNotFoundException.class);
        assertThatThrownBy(() -> service.createInstantOrderBid("buyer@test.com", 71L, 3L, false, pay))
                .isInstanceOf(InvalidOrderBidDataException.class);
        assertThatThrownBy(() -> service.createInstantOrderBid("buyer@test.com", 70L, null, false, pay))
                .isInstanceOf(InvalidOrderBidDataException.class);
        verifyNoInteractions(orderCommandService);
    }

    @Test
    void deleteOrderBidGuardsSaleAndMatchedThenDeletesOrderAndBid() {
        OrderBid withSale = OrderBid.builder().id(1L).sale(Sale.builder().build()).status(BidStatus.PENDING).build();
        OrderBid matched = OrderBid.builder().id(2L).status(BidStatus.MATCHED).build();
        Order order = Order.builder().id(300L).build();
        OrderBid pendingWithOrder = OrderBid.builder().id(3L).status(BidStatus.PENDING).order(order).build();
        when(orderBidRepository.findById(1L)).thenReturn(Optional.of(withSale));
        when(orderBidRepository.findById(2L)).thenReturn(Optional.of(matched));
        when(orderBidRepository.findById(3L)).thenReturn(Optional.of(pendingWithOrder));
        when(orderBidRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteOrderBid(1L))
                .isInstanceOf(OrderBidAlreadyMatchedException.class)
                .hasMessage("주문 입찰이 판매와 연결되어 있어 삭제할 수 없습니다.");
        assertThatThrownBy(() -> service.deleteOrderBid(2L))
                .isInstanceOf(OrderBidAlreadyMatchedException.class)
                .hasMessage("이미 매칭된 주문 입찰은 삭제할 수 없습니다.");
        assertThatThrownBy(() -> service.deleteOrderBid(4L))
                .isInstanceOf(OrderBidNotFoundException.class);

        service.deleteOrderBid(3L);

        verify(orderCommandService).deleteOrder(300L);
        verify(orderBidRepository).delete(pendingWithOrder);
        verify(orderBidRepository, never()).delete(withSale);
        verify(orderBidRepository, never()).delete(matched);
    }

    @Test
    void matchOrderBidOnlyFromNonMatched() {
        OrderBid pending = OrderBid.builder().id(1L).status(BidStatus.PENDING).build();
        OrderBid cancelled = OrderBid.builder().id(2L).status(BidStatus.CANCELLED).build();
        OrderBid matched = OrderBid.builder().id(3L).status(BidStatus.MATCHED).build();

        service.matchOrderBid(pending);
        service.matchOrderBid(cancelled);

        assertThat(pending.getStatus()).isEqualTo(BidStatus.MATCHED);
        assertThat(cancelled.getStatus()).isEqualTo(BidStatus.MATCHED);
        assertThatThrownBy(() -> service.matchOrderBid(matched))
                .isInstanceOf(OrderBidAlreadyMatchedException.class);
        assertThatThrownBy(() -> service.matchOrderBid(null))
                .isInstanceOf(OrderBidNotFoundException.class);
    }
}
