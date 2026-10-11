package com.fream.back.visible.c04;

import com.fream.back.domain.faq.entity.BidStatus;
import com.fream.back.domain.faq.entity.Faq;
import com.fream.back.domain.faq.entity.FaqBid;
import com.fream.back.domain.faq.repository.FaqBidRepository;
import com.fream.back.domain.faq.service.command.FaqBidCommandService;
import com.fream.back.domain.faq.service.command.FaqCommandService;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.user.service.productSize.UserSizeQueryService;
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




@ExtendWith(MockitoExtension.class)
class PurchaseBidRegistrationTest {

    @Mock FaqBidRepository orderBidRepository;
    @Mock FaqCommandService orderCommandService;
    @Mock UserSizeQueryService productSizeQueryService;
    @Mock ProductQueryService userQueryService;
    @Mock StyleBidQueryService saleBidQueryService;

    @InjectMocks FaqBidCommandService service;

    private final Product buyer = Product.builder().id(1L).email("buyer@test.com").build();
    private final UserSize size = UserSize.builder().id(10L).size("260").build();

    private FaqBid register() {
        Faq order = Faq.builder().id(100L).user(buyer).build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(productSizeQueryService.findById(10L)).thenReturn(Optional.of(size));
        when(orderCommandService.createOrderFromBid(buyer, size, 180000)).thenReturn(order);
        when(orderBidRepository.save(any(FaqBid.class))).thenAnswer(inv -> inv.getArgument(0));
        return service.createOrderBid("buyer@test.com", 10L, 180000);
    }

    @Test
    void newPurchaseBidIsWaitingForASeller() {
        FaqBid bid = register();

        assertThat(bid.getStatus()).isEqualTo(BidStatus.PENDING);
        assertThat(bid.getSale()).isNull();
        assertThat(bid.isInstantPurchase()).isFalse();
    }

    @Test
    void newPurchaseBidCanBeCancelled() {
        FaqBid bid = register();
        when(orderBidRepository.findById(55L)).thenReturn(Optional.of(bid));

        assertThatCode(() -> service.deleteOrderBid(55L)).doesNotThrowAnyException();
        verify(orderBidRepository).delete(bid);
    }
}
