package com.fream.back.domain.sale.service;

import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.service.query.OrderBidQueryService;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.product.service.productSize.ProductSizeQueryService;
import com.fream.back.domain.sale.entity.BidStatus;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleBid;
import com.fream.back.domain.sale.entity.SaleStatus;
import com.fream.back.domain.sale.repository.SaleBidRepository;
import com.fream.back.domain.sale.service.command.SaleBidCommandService;
import com.fream.back.domain.sale.service.command.SaleCommandService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.service.query.UserQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * SaleBidCommandService의 판매 입찰 생성·즉시 판매·삭제 흐름을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class SaleBidCommandServiceTest {

    @Mock SaleBidRepository saleBidRepository;
    @Mock UserQueryService userQueryService;
    @Mock ProductSizeQueryService productSizeQueryService;
    @Mock SaleCommandService saleCommandService;
    @Mock OrderBidQueryService orderBidQueryService;

    @InjectMocks SaleBidCommandService saleBidCommandService;

    private final User seller = User.builder().id(1L).email("seller@test.com").build();
    private final ProductSize productSize = ProductSize.builder().id(7L).size("260").build();

    @Test
    void createSaleBidCreatesSaleAndBidAndEndsMatched() {
        Sale sale = Sale.builder().id(30L).seller(seller).productSize(productSize).status(SaleStatus.PENDING_SHIPMENT).build();
        when(userQueryService.findByEmail("seller@test.com")).thenReturn(seller);
        when(productSizeQueryService.findById(7L)).thenReturn(Optional.of(productSize));
        when(saleCommandService.createSale(seller, productSize, "주소", "11111", "010", true)).thenReturn(sale);
        when(saleBidRepository.save(any(SaleBid.class))).thenAnswer(inv -> inv.getArgument(0));

        SaleBid bid = saleBidCommandService.createSaleBid("seller@test.com", 7L, 200000, "주소", "11111", "010", true);

        assertThat(bid.getSeller()).isSameAs(seller);
        assertThat(bid.getProductSize()).isSameAs(productSize);
        assertThat(bid.getBidPrice()).isEqualTo(200000);
        assertThat(bid.getSale()).isSameAs(sale);
        assertThat(sale.getSaleBid()).isSameAs(bid);
        // 저장 시점에는 PENDING이지만 sale.assignSaleBid 이후 반환값은 MATCHED
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
        assertThat(bid.isInstantSale()).isFalse();
    }

    @Test
    void createSaleBidSavesBidWhileStillPending() {
        Sale sale = Sale.builder().id(30L).build();
        when(userQueryService.findByEmail("seller@test.com")).thenReturn(seller);
        when(productSizeQueryService.findById(7L)).thenReturn(Optional.of(productSize));
        when(saleCommandService.createSale(any(), any(), any(), any(), any(), anyBoolean())).thenReturn(sale);
        BidStatus[] statusAtSave = new BidStatus[1];
        when(saleBidRepository.save(any(SaleBid.class))).thenAnswer(inv -> {
            SaleBid b = inv.getArgument(0);
            statusAtSave[0] = b.getStatus();
            return b;
        });

        saleBidCommandService.createSaleBid("seller@test.com", 7L, 1000, "a", "b", "c", false);

        assertThat(statusAtSave[0]).isEqualTo(BidStatus.PENDING);
    }

    @Test
    void createSaleBidFailsWhenProductSizeMissingBeforeCreatingSale() {
        when(userQueryService.findByEmail("seller@test.com")).thenReturn(seller);
        when(productSizeQueryService.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleBidCommandService.createSaleBid("seller@test.com", 8L, 1000, "a", "b", "c", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 상품 사이즈를 찾을 수 없습니다: 8");
        verifyNoInteractions(saleCommandService, saleBidRepository);
    }

    @Test
    void createInstantSaleBidUsesOrderBidPriceAndOrderAndReturnsSaleId() {
        Order order = Order.builder().id(50L).build();
        OrderBid orderBid = OrderBid.builder().id(5L).bidPrice(180000).order(order).build();
        Sale sale = Sale.builder().id(31L).seller(seller).productSize(productSize).status(SaleStatus.PENDING_SHIPMENT).build();
        when(saleCommandService.createInstantSale(5L, "seller@test.com", "주소", "11111", "010")).thenReturn(sale);
        when(orderBidQueryService.findById(5L)).thenReturn(Optional.of(orderBid));

        Long saleId = saleBidCommandService.createInstantSaleBid(5L, "seller@test.com", "주소", "11111", "010");

        assertThat(saleId).isEqualTo(31L);
        ArgumentCaptor<SaleBid> captor = ArgumentCaptor.forClass(SaleBid.class);
        verify(saleBidRepository).save(captor.capture());
        SaleBid saved = captor.getValue();
        assertThat(saved.getBidPrice()).isEqualTo(180000);
        assertThat(saved.getSeller()).isSameAs(seller);
        assertThat(saved.getProductSize()).isSameAs(productSize);
        assertThat(saved.getSale()).isSameAs(sale);
        assertThat(saved.getOrder()).isSameAs(order);
        assertThat(saved.getStatus()).isEqualTo(BidStatus.MATCHED);
        assertThat(saved.isInstantSale()).isFalse();
        assertThat(sale.getSaleBid()).isSameAs(saved);
    }

    @Test
    void createInstantSaleBidCallsSaleCreationBeforeOrderBidLookup() {
        when(saleCommandService.createInstantSale(5L, "s", "a", "p", "r")).thenReturn(Sale.builder().id(1L).build());
        when(orderBidQueryService.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleBidCommandService.createInstantSaleBid(5L, "s", "a", "p", "r"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 OrderBid를 찾을 수 없습니다.");
        verify(saleCommandService).createInstantSale(5L, "s", "a", "p", "r");
        verify(saleBidRepository, never()).save(any());
    }

    @Test
    void deleteSaleBidRejectsBidLinkedToOrder() {
        SaleBid bid = SaleBid.builder().id(9L).order(Order.builder().id(50L).build()).build();
        when(saleBidRepository.findById(9L)).thenReturn(Optional.of(bid));

        assertThatThrownBy(() -> saleBidCommandService.deleteSaleBid(9L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("SaleBid는 Order와 연결되어 있으므로 삭제할 수 없습니다.");
        verify(saleBidRepository, never()).delete(any());
        verifyNoInteractions(saleCommandService);
    }

    @Test
    void deleteSaleBidDeletesLinkedSaleThenBid() {
        SaleBid bid = SaleBid.builder().id(9L).sale(Sale.builder().id(30L).build()).build();
        when(saleBidRepository.findById(9L)).thenReturn(Optional.of(bid));

        saleBidCommandService.deleteSaleBid(9L);

        var order = inOrder(saleCommandService, saleBidRepository);
        order.verify(saleCommandService).deleteSale(30L);
        order.verify(saleBidRepository).delete(bid);
    }

    @Test
    void deleteSaleBidWithoutSaleOnlyDeletesBid() {
        SaleBid bid = SaleBid.builder().id(9L).build();
        when(saleBidRepository.findById(9L)).thenReturn(Optional.of(bid));

        saleBidCommandService.deleteSaleBid(9L);

        verify(saleCommandService, never()).deleteSale(anyLong());
        verify(saleBidRepository).delete(bid);
    }

    @Test
    void deleteSaleBidFailsWhenMissing() {
        when(saleBidRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleBidCommandService.deleteSaleBid(9L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 SaleBid를 찾을 수 없습니다: 9");
    }
}
