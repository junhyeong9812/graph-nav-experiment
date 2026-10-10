package com.fream.back.hidden.c04;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 지점 2: 일반 판매 입찰 등록. */
@ExtendWith(MockitoExtension.class)
class SaleBidRegistrationHiddenTest {

    @Mock SaleBidRepository saleBidRepository;
    @Mock UserQueryService userQueryService;
    @Mock ProductSizeQueryService productSizeQueryService;
    @Mock SaleCommandService saleCommandService;
    @Mock OrderBidQueryService orderBidQueryService;

    @InjectMocks SaleBidCommandService service;

    private final User seller = User.builder().id(1L).email("seller@test.com").build();
    private final ProductSize size = ProductSize.builder().id(7L).size("260").build();

    @Test
    void registeredSaleBidIsPendingAndKeepsItsSale() {
        Sale sale = Sale.builder().id(30L).seller(seller).productSize(size).status(SaleStatus.PENDING_SHIPMENT).build();
        when(userQueryService.findByEmail("seller@test.com")).thenReturn(seller);
        when(productSizeQueryService.findById(7L)).thenReturn(Optional.of(size));
        when(saleCommandService.createSale(seller, size, "주소", "11111", "010", true)).thenReturn(sale);
        when(saleBidRepository.save(any(SaleBid.class))).thenAnswer(inv -> inv.getArgument(0));

        SaleBid bid = service.createSaleBid("seller@test.com", 7L, 200000, "주소", "11111", "010", true);

        assertThat(bid.getStatus()).isEqualTo(BidStatus.PENDING);
        assertThat(bid.getSale()).isSameAs(sale);
        assertThat(bid.getSeller()).isSameAs(seller);
        assertThat(bid.getBidPrice()).isEqualTo(200000);
        assertThat(bid.getOrder()).isNull();
        assertThat(bid.isInstantSale()).isFalse();
    }

    @Test
    void instantSaleIsStillMatchedRightAway() {
        Order order = Order.builder().id(50L).build();
        OrderBid orderBid = OrderBid.builder().id(5L).bidPrice(180000).order(order).build();
        Sale sale = Sale.builder().id(31L).seller(seller).productSize(size).status(SaleStatus.PENDING_SHIPMENT).build();
        when(saleCommandService.createInstantSale(5L, "seller@test.com", "주소", "11111", "010")).thenReturn(sale);
        when(orderBidQueryService.findById(5L)).thenReturn(Optional.of(orderBid));

        Long saleId = service.createInstantSaleBid(5L, "seller@test.com", "주소", "11111", "010");

        assertThat(saleId).isEqualTo(31L);
        ArgumentCaptor<SaleBid> captor = ArgumentCaptor.forClass(SaleBid.class);
        verify(saleBidRepository).save(captor.capture());
        SaleBid saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(BidStatus.MATCHED);
        assertThat(saved.getOrder()).isSameAs(order);
        assertThat(saved.getSale()).isSameAs(sale);
    }
}
