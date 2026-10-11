package com.fream.back.hidden.c04;

import com.fream.back.domain.m99.entity.Xkh73Status;
import com.fream.back.domain.m99.entity.Xhq12;
import com.fream.back.domain.m99.entity.Xtw92;
import com.fream.back.domain.m99.repository.Xtw92Repository;
import com.fream.back.domain.m99.service.command.Xtw92CommandService;
import com.fream.back.domain.m99.service.command.Xhq12CommandService;
import com.fream.back.domain.m64.dto.Xgg74RequestDto;
import com.fream.back.domain.m64.dto.Xjt45RequestDto;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m30.service.p11.Xcc44QueryService;
import com.fream.back.domain.m85.entity.Xpj42;
import com.fream.back.domain.m85.entity.Xzt63;
import com.fream.back.domain.m85.service.query.Xzt63QueryService;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.domain.m22.service.query.Xjm45QueryService;
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

    @Mock Xtw92Repository orderBidRepository;
    @Mock Xhq12CommandService orderCommandService;
    @Mock Xcc44QueryService productSizeQueryService;
    @Mock Xjm45QueryService userQueryService;
    @Mock Xzt63QueryService saleBidQueryService;

    @InjectMocks Xtw92CommandService service;

    private final Xjm45 buyer = Xjm45.builder().id(1L).email("buyer@test.com").build();
    private final Xcc44 size = Xcc44.builder().id(10L).size("260").build();
    private final Xhq12 order = Xhq12.builder().id(100L).user(buyer).build();

    private Xtw92 register() {
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(productSizeQueryService.findById(10L)).thenReturn(Optional.of(size));
        when(orderCommandService.createOrderFromBid(buyer, size, 180000)).thenReturn(order);
        when(orderBidRepository.save(any(Xtw92.class))).thenAnswer(inv -> inv.getArgument(0));
        return service.createOrderBid("buyer@test.com", 10L, 180000);
    }

    @Test
    void registeredBidIsPendingAndKeepsItsOrder() {
        Xtw92 bid = register();

        assertThat(bid.getStatus()).isEqualTo(Xkh73Status.PENDING);
        assertThat(bid.getOrder()).isSameAs(order);
        assertThat(bid.getUser()).isSameAs(buyer);
        assertThat(bid.getBidPrice()).isEqualTo(180000);
        assertThat(bid.getSale()).isNull();
        assertThat(bid.isInstantPurchase()).isFalse();
    }

    @Test
    void registeredBidCanBeCancelledTogetherWithItsOrder() {
        Xtw92 bid = register();
        when(orderBidRepository.findById(55L)).thenReturn(Optional.of(bid));

        assertThatCode(() -> service.deleteOrderBid(55L)).doesNotThrowAnyException();
        verify(orderCommandService).deleteOrder(100L);
        verify(orderBidRepository).delete(bid);
    }

    @Test
    void registeredBidCanLaterBeMatched() {
        Xtw92 bid = register();

        assertThatCode(() -> service.matchOrderBid(bid)).doesNotThrowAnyException();
        assertThat(bid.getStatus()).isEqualTo(Xkh73Status.MATCHED);
    }

    @Test
    void instantPurchaseIsStillMatchedRightAway() {
        Xjm45 seller = Xjm45.builder().id(2L).build();
        Xpj42 sale = Xpj42.builder().id(50L).seller(seller).build();
        Xzt63 saleBid = Xzt63.builder().id(70L).bidPrice(210000).productSize(size).sale(sale).build();
        Xhq12 instantOrder = Xhq12.builder().id(101L).user(buyer).build();
        Xjt45RequestDto pay = Xgg74RequestDto.builder().impUid("imp").build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(saleBidQueryService.findById(70L)).thenReturn(saleBid);
        when(orderCommandService.createInstantOrder(buyer, saleBid, 3L, true, pay)).thenReturn(instantOrder);
        when(orderBidRepository.save(any(Xtw92.class))).thenAnswer(inv -> inv.getArgument(0));

        Xtw92 bid = service.createInstantOrderBid("buyer@test.com", 70L, 3L, true, pay);

        assertThat(bid.getStatus()).isEqualTo(Xkh73Status.MATCHED);
        assertThat(bid.isInstantPurchase()).isTrue();
        assertThat(bid.getOrder()).isSameAs(instantOrder);
        assertThat(bid.getSale()).isSameAs(sale);
    }
}
