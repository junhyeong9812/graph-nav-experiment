package com.fream.back.hidden.c04;

import com.fream.back.domain.m16.entity.Xtz73Status;
import com.fream.back.domain.m16.entity.Xdg64;
import com.fream.back.domain.m16.entity.Xzj27;
import com.fream.back.domain.m16.repository.Xzj27Repository;
import com.fream.back.domain.m16.service.command.Xzj27CommandService;
import com.fream.back.domain.m16.service.command.Xdg64CommandService;
import com.fream.back.domain.m06.dto.Xjt86RequestDto;
import com.fream.back.domain.m06.dto.Xbf54RequestDto;
import com.fream.back.domain.m02.entity.Xdh20;
import com.fream.back.domain.m02.service.p14.Xdh20QueryService;
import com.fream.back.domain.m10.entity.Xgg74;
import com.fream.back.domain.m10.entity.Xzz58;
import com.fream.back.domain.m10.service.query.Xzz58QueryService;
import com.fream.back.domain.m13.entity.Xrd81;
import com.fream.back.domain.m13.service.query.Xrd81QueryService;
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

    @Mock Xzj27Repository orderBidRepository;
    @Mock Xdg64CommandService orderCommandService;
    @Mock Xdh20QueryService productSizeQueryService;
    @Mock Xrd81QueryService userQueryService;
    @Mock Xzz58QueryService saleBidQueryService;

    @InjectMocks Xzj27CommandService service;

    private final Xrd81 buyer = Xrd81.builder().id(1L).email("buyer@test.com").build();
    private final Xdh20 size = Xdh20.builder().id(10L).size("260").build();
    private final Xdg64 order = Xdg64.builder().id(100L).user(buyer).build();

    private Xzj27 register() {
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(productSizeQueryService.findById(10L)).thenReturn(Optional.of(size));
        when(orderCommandService.createOrderFromBid(buyer, size, 180000)).thenReturn(order);
        when(orderBidRepository.save(any(Xzj27.class))).thenAnswer(inv -> inv.getArgument(0));
        return service.createOrderBid("buyer@test.com", 10L, 180000);
    }

    @Test
    void registeredBidIsPendingAndKeepsItsOrder() {
        Xzj27 bid = register();

        assertThat(bid.getStatus()).isEqualTo(Xtz73Status.PENDING);
        assertThat(bid.getOrder()).isSameAs(order);
        assertThat(bid.getUser()).isSameAs(buyer);
        assertThat(bid.getBidPrice()).isEqualTo(180000);
        assertThat(bid.getSale()).isNull();
        assertThat(bid.isInstantPurchase()).isFalse();
    }

    @Test
    void registeredBidCanBeCancelledTogetherWithItsOrder() {
        Xzj27 bid = register();
        when(orderBidRepository.findById(55L)).thenReturn(Optional.of(bid));

        assertThatCode(() -> service.deleteOrderBid(55L)).doesNotThrowAnyException();
        verify(orderCommandService).deleteOrder(100L);
        verify(orderBidRepository).delete(bid);
    }

    @Test
    void registeredBidCanLaterBeMatched() {
        Xzj27 bid = register();

        assertThatCode(() -> service.matchOrderBid(bid)).doesNotThrowAnyException();
        assertThat(bid.getStatus()).isEqualTo(Xtz73Status.MATCHED);
    }

    @Test
    void instantPurchaseIsStillMatchedRightAway() {
        Xrd81 seller = Xrd81.builder().id(2L).build();
        Xgg74 sale = Xgg74.builder().id(50L).seller(seller).build();
        Xzz58 saleBid = Xzz58.builder().id(70L).bidPrice(210000).productSize(size).sale(sale).build();
        Xdg64 instantOrder = Xdg64.builder().id(101L).user(buyer).build();
        Xbf54RequestDto pay = Xjt86RequestDto.builder().impUid("imp").build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(saleBidQueryService.findById(70L)).thenReturn(saleBid);
        when(orderCommandService.createInstantOrder(buyer, saleBid, 3L, true, pay)).thenReturn(instantOrder);
        when(orderBidRepository.save(any(Xzj27.class))).thenAnswer(inv -> inv.getArgument(0));

        Xzj27 bid = service.createInstantOrderBid("buyer@test.com", 70L, 3L, true, pay);

        assertThat(bid.getStatus()).isEqualTo(Xtz73Status.MATCHED);
        assertThat(bid.isInstantPurchase()).isTrue();
        assertThat(bid.getOrder()).isSameAs(instantOrder);
        assertThat(bid.getSale()).isSameAs(sale);
    }
}
