package com.fream.back.hidden.c04;

import com.fream.back.domain.m16.entity.Xdg64;
import com.fream.back.domain.m16.entity.Xzj27;
import com.fream.back.domain.m16.service.query.Xzj27QueryService;
import com.fream.back.domain.m02.entity.Xdh20;
import com.fream.back.domain.m02.service.p14.Xdh20QueryService;
import com.fream.back.domain.m10.entity.Xtz73Status;
import com.fream.back.domain.m10.entity.Xgg74;
import com.fream.back.domain.m10.entity.Xzz58;
import com.fream.back.domain.m10.entity.Xgg74Status;
import com.fream.back.domain.m10.repository.Xzz58Repository;
import com.fream.back.domain.m10.service.command.Xzz58CommandService;
import com.fream.back.domain.m10.service.command.Xgg74CommandService;
import com.fream.back.domain.m13.entity.Xrd81;
import com.fream.back.domain.m13.service.query.Xrd81QueryService;
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

    @Mock Xzz58Repository saleBidRepository;
    @Mock Xrd81QueryService userQueryService;
    @Mock Xdh20QueryService productSizeQueryService;
    @Mock Xgg74CommandService saleCommandService;
    @Mock Xzj27QueryService orderBidQueryService;

    @InjectMocks Xzz58CommandService service;

    private final Xrd81 seller = Xrd81.builder().id(1L).email("seller@test.com").build();
    private final Xdh20 size = Xdh20.builder().id(7L).size("260").build();

    @Test
    void registeredSaleBidIsPendingAndKeepsItsSale() {
        Xgg74 sale = Xgg74.builder().id(30L).seller(seller).productSize(size).status(Xgg74Status.PENDING_SHIPMENT).build();
        when(userQueryService.findByEmail("seller@test.com")).thenReturn(seller);
        when(productSizeQueryService.findById(7L)).thenReturn(Optional.of(size));
        when(saleCommandService.createSale(seller, size, "주소", "11111", "010", true)).thenReturn(sale);
        when(saleBidRepository.save(any(Xzz58.class))).thenAnswer(inv -> inv.getArgument(0));

        Xzz58 bid = service.createSaleBid("seller@test.com", 7L, 200000, "주소", "11111", "010", true);

        assertThat(bid.getStatus()).isEqualTo(Xtz73Status.PENDING);
        assertThat(bid.getSale()).isSameAs(sale);
        assertThat(bid.getSeller()).isSameAs(seller);
        assertThat(bid.getBidPrice()).isEqualTo(200000);
        assertThat(bid.getOrder()).isNull();
        assertThat(bid.isInstantSale()).isFalse();
    }

    @Test
    void instantSaleIsStillMatchedRightAway() {
        Xdg64 order = Xdg64.builder().id(50L).build();
        Xzj27 orderBid = Xzj27.builder().id(5L).bidPrice(180000).order(order).build();
        Xgg74 sale = Xgg74.builder().id(31L).seller(seller).productSize(size).status(Xgg74Status.PENDING_SHIPMENT).build();
        when(saleCommandService.createInstantSale(5L, "seller@test.com", "주소", "11111", "010")).thenReturn(sale);
        when(orderBidQueryService.findById(5L)).thenReturn(Optional.of(orderBid));

        Long saleId = service.createInstantSaleBid(5L, "seller@test.com", "주소", "11111", "010");

        assertThat(saleId).isEqualTo(31L);
        ArgumentCaptor<Xzz58> captor = ArgumentCaptor.forClass(Xzz58.class);
        verify(saleBidRepository).save(captor.capture());
        Xzz58 saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(Xtz73Status.MATCHED);
        assertThat(saved.getOrder()).isSameAs(order);
        assertThat(saved.getSale()).isSameAs(sale);
    }
}
