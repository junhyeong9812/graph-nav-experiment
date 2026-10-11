package com.fream.back.hidden.c04;

import com.fream.back.domain.faq.entity.Faq;
import com.fream.back.domain.faq.entity.FaqBid;
import com.fream.back.domain.faq.service.query.FaqBidQueryService;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.user.service.productSize.UserSizeQueryService;
import com.fream.back.domain.style.entity.BidStatus;
import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleBid;
import com.fream.back.domain.style.entity.StyleStatus;
import com.fream.back.domain.style.repository.StyleBidRepository;
import com.fream.back.domain.style.service.command.StyleBidCommandService;
import com.fream.back.domain.style.service.command.StyleCommandService;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.service.query.ProductQueryService;
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

    @Mock StyleBidRepository saleBidRepository;
    @Mock ProductQueryService userQueryService;
    @Mock UserSizeQueryService productSizeQueryService;
    @Mock StyleCommandService saleCommandService;
    @Mock FaqBidQueryService orderBidQueryService;

    @InjectMocks StyleBidCommandService service;

    private final Product seller = Product.builder().id(1L).email("seller@test.com").build();
    private final UserSize size = UserSize.builder().id(7L).size("260").build();

    @Test
    void registeredSaleBidIsPendingAndKeepsItsSale() {
        Style sale = Style.builder().id(30L).seller(seller).productSize(size).status(StyleStatus.PENDING_SHIPMENT).build();
        when(userQueryService.findByEmail("seller@test.com")).thenReturn(seller);
        when(productSizeQueryService.findById(7L)).thenReturn(Optional.of(size));
        when(saleCommandService.createSale(seller, size, "주소", "11111", "010", true)).thenReturn(sale);
        when(saleBidRepository.save(any(StyleBid.class))).thenAnswer(inv -> inv.getArgument(0));

        StyleBid bid = service.createSaleBid("seller@test.com", 7L, 200000, "주소", "11111", "010", true);

        assertThat(bid.getStatus()).isEqualTo(BidStatus.PENDING);
        assertThat(bid.getSale()).isSameAs(sale);
        assertThat(bid.getSeller()).isSameAs(seller);
        assertThat(bid.getBidPrice()).isEqualTo(200000);
        assertThat(bid.getOrder()).isNull();
        assertThat(bid.isInstantSale()).isFalse();
    }

    @Test
    void instantSaleIsStillMatchedRightAway() {
        Faq order = Faq.builder().id(50L).build();
        FaqBid orderBid = FaqBid.builder().id(5L).bidPrice(180000).order(order).build();
        Style sale = Style.builder().id(31L).seller(seller).productSize(size).status(StyleStatus.PENDING_SHIPMENT).build();
        when(saleCommandService.createInstantSale(5L, "seller@test.com", "주소", "11111", "010")).thenReturn(sale);
        when(orderBidQueryService.findById(5L)).thenReturn(Optional.of(orderBid));

        Long saleId = service.createInstantSaleBid(5L, "seller@test.com", "주소", "11111", "010");

        assertThat(saleId).isEqualTo(31L);
        ArgumentCaptor<StyleBid> captor = ArgumentCaptor.forClass(StyleBid.class);
        verify(saleBidRepository).save(captor.capture());
        StyleBid saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(BidStatus.MATCHED);
        assertThat(saved.getOrder()).isSameAs(order);
        assertThat(saved.getSale()).isSameAs(sale);
    }
}
