package com.fream.back.hidden.c04;

import com.fream.back.domain.faq.entity.BidStatus;
import com.fream.back.domain.faq.entity.Faq;
import com.fream.back.domain.faq.entity.FaqBid;
import com.fream.back.domain.faq.repository.FaqBidRepository;
import com.fream.back.domain.faq.service.command.FaqBidCommandService;
import com.fream.back.domain.faq.service.command.FaqCommandService;
import com.fream.back.domain.notification.dto.GeneralNotificationRequestDto;
import com.fream.back.domain.notification.dto.NotificationRequestDto;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.user.service.productSize.UserSizeQueryService;
import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleBid;
import com.fream.back.domain.style.service.query.StyleBidQueryService;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.service.query.ProductQueryService;
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

    @Mock FaqBidRepository orderBidRepository;
    @Mock FaqCommandService orderCommandService;
    @Mock UserSizeQueryService productSizeQueryService;
    @Mock ProductQueryService userQueryService;
    @Mock StyleBidQueryService saleBidQueryService;

    @InjectMocks FaqBidCommandService service;

    private final Product buyer = Product.builder().id(1L).email("buyer@test.com").build();
    private final UserSize size = UserSize.builder().id(10L).size("260").build();
    private final Faq order = Faq.builder().id(100L).user(buyer).build();

    private FaqBid register() {
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(productSizeQueryService.findById(10L)).thenReturn(Optional.of(size));
        when(orderCommandService.createOrderFromBid(buyer, size, 180000)).thenReturn(order);
        when(orderBidRepository.save(any(FaqBid.class))).thenAnswer(inv -> inv.getArgument(0));
        return service.createOrderBid("buyer@test.com", 10L, 180000);
    }

    @Test
    void registeredBidIsPendingAndKeepsItsOrder() {
        FaqBid bid = register();

        assertThat(bid.getStatus()).isEqualTo(BidStatus.PENDING);
        assertThat(bid.getOrder()).isSameAs(order);
        assertThat(bid.getUser()).isSameAs(buyer);
        assertThat(bid.getBidPrice()).isEqualTo(180000);
        assertThat(bid.getSale()).isNull();
        assertThat(bid.isInstantPurchase()).isFalse();
    }

    @Test
    void registeredBidCanBeCancelledTogetherWithItsOrder() {
        FaqBid bid = register();
        when(orderBidRepository.findById(55L)).thenReturn(Optional.of(bid));

        assertThatCode(() -> service.deleteOrderBid(55L)).doesNotThrowAnyException();
        verify(orderCommandService).deleteOrder(100L);
        verify(orderBidRepository).delete(bid);
    }

    @Test
    void registeredBidCanLaterBeMatched() {
        FaqBid bid = register();

        assertThatCode(() -> service.matchOrderBid(bid)).doesNotThrowAnyException();
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
    }

    @Test
    void instantPurchaseIsStillMatchedRightAway() {
        Product seller = Product.builder().id(2L).build();
        Style sale = Style.builder().id(50L).seller(seller).build();
        StyleBid saleBid = StyleBid.builder().id(70L).bidPrice(210000).productSize(size).sale(sale).build();
        Faq instantOrder = Faq.builder().id(101L).user(buyer).build();
        NotificationRequestDto pay = GeneralNotificationRequestDto.builder().impUid("imp").build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(saleBidQueryService.findById(70L)).thenReturn(saleBid);
        when(orderCommandService.createInstantOrder(buyer, saleBid, 3L, true, pay)).thenReturn(instantOrder);
        when(orderBidRepository.save(any(FaqBid.class))).thenAnswer(inv -> inv.getArgument(0));

        FaqBid bid = service.createInstantOrderBid("buyer@test.com", 70L, 3L, true, pay);

        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
        assertThat(bid.isInstantPurchase()).isTrue();
        assertThat(bid.getOrder()).isSameAs(instantOrder);
        assertThat(bid.getSale()).isSameAs(sale);
    }
}
