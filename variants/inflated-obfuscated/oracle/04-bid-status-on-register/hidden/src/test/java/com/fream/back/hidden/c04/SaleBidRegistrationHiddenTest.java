package com.fream.back.hidden.c04;

import com.fream.back.domain.m99.entity.Xhq12;
import com.fream.back.domain.m99.entity.Xtw92;
import com.fream.back.domain.m99.service.query.Xtw92QueryService;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m30.service.p11.Xcc44QueryService;
import com.fream.back.domain.m85.entity.Xkh73Status;
import com.fream.back.domain.m85.entity.Xpj42;
import com.fream.back.domain.m85.entity.Xzt63;
import com.fream.back.domain.m85.entity.Xpj42Status;
import com.fream.back.domain.m85.repository.Xzt63Repository;
import com.fream.back.domain.m85.service.command.Xzt63CommandService;
import com.fream.back.domain.m85.service.command.Xpj42CommandService;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.domain.m22.service.query.Xjm45QueryService;
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

    @Mock Xzt63Repository saleBidRepository;
    @Mock Xjm45QueryService userQueryService;
    @Mock Xcc44QueryService productSizeQueryService;
    @Mock Xpj42CommandService saleCommandService;
    @Mock Xtw92QueryService orderBidQueryService;

    @InjectMocks Xzt63CommandService service;

    private final Xjm45 seller = Xjm45.builder().id(1L).email("seller@test.com").build();
    private final Xcc44 size = Xcc44.builder().id(7L).size("260").build();

    @Test
    void registeredSaleBidIsPendingAndKeepsItsSale() {
        Xpj42 sale = Xpj42.builder().id(30L).seller(seller).productSize(size).status(Xpj42Status.PENDING_SHIPMENT).build();
        when(userQueryService.findByEmail("seller@test.com")).thenReturn(seller);
        when(productSizeQueryService.findById(7L)).thenReturn(Optional.of(size));
        when(saleCommandService.createSale(seller, size, "주소", "11111", "010", true)).thenReturn(sale);
        when(saleBidRepository.save(any(Xzt63.class))).thenAnswer(inv -> inv.getArgument(0));

        Xzt63 bid = service.createSaleBid("seller@test.com", 7L, 200000, "주소", "11111", "010", true);

        assertThat(bid.getStatus()).isEqualTo(Xkh73Status.PENDING);
        assertThat(bid.getSale()).isSameAs(sale);
        assertThat(bid.getSeller()).isSameAs(seller);
        assertThat(bid.getBidPrice()).isEqualTo(200000);
        assertThat(bid.getOrder()).isNull();
        assertThat(bid.isInstantSale()).isFalse();
    }

    @Test
    void instantSaleIsStillMatchedRightAway() {
        Xhq12 order = Xhq12.builder().id(50L).build();
        Xtw92 orderBid = Xtw92.builder().id(5L).bidPrice(180000).order(order).build();
        Xpj42 sale = Xpj42.builder().id(31L).seller(seller).productSize(size).status(Xpj42Status.PENDING_SHIPMENT).build();
        when(saleCommandService.createInstantSale(5L, "seller@test.com", "주소", "11111", "010")).thenReturn(sale);
        when(orderBidQueryService.findById(5L)).thenReturn(Optional.of(orderBid));

        Long saleId = service.createInstantSaleBid(5L, "seller@test.com", "주소", "11111", "010");

        assertThat(saleId).isEqualTo(31L);
        ArgumentCaptor<Xzt63> captor = ArgumentCaptor.forClass(Xzt63.class);
        verify(saleBidRepository).save(captor.capture());
        Xzt63 saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(Xkh73Status.MATCHED);
        assertThat(saved.getOrder()).isSameAs(order);
        assertThat(saved.getSale()).isSameAs(sale);
    }
}
