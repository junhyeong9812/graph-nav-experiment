package com.fream.back.visible.c04;

import com.fream.back.domain.m16.entity.Xtz73Status;
import com.fream.back.domain.m16.entity.Xdg64;
import com.fream.back.domain.m16.entity.Xzj27;
import com.fream.back.domain.m16.repository.Xzj27Repository;
import com.fream.back.domain.m16.service.command.Xzj27CommandService;
import com.fream.back.domain.m16.service.command.Xdg64CommandService;
import com.fream.back.domain.m02.entity.Xdh20;
import com.fream.back.domain.m02.service.p14.Xdh20QueryService;
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




@ExtendWith(MockitoExtension.class)
class PurchaseBidRegistrationTest {

    @Mock Xzj27Repository orderBidRepository;
    @Mock Xdg64CommandService orderCommandService;
    @Mock Xdh20QueryService productSizeQueryService;
    @Mock Xrd81QueryService userQueryService;
    @Mock Xzz58QueryService saleBidQueryService;

    @InjectMocks Xzj27CommandService service;

    private final Xrd81 buyer = Xrd81.builder().id(1L).email("buyer@test.com").build();
    private final Xdh20 size = Xdh20.builder().id(10L).size("260").build();

    private Xzj27 register() {
        Xdg64 order = Xdg64.builder().id(100L).user(buyer).build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(productSizeQueryService.findById(10L)).thenReturn(Optional.of(size));
        when(orderCommandService.createOrderFromBid(buyer, size, 180000)).thenReturn(order);
        when(orderBidRepository.save(any(Xzj27.class))).thenAnswer(inv -> inv.getArgument(0));
        return service.createOrderBid("buyer@test.com", 10L, 180000);
    }

    @Test
    void newPurchaseBidIsWaitingForASeller() {
        Xzj27 bid = register();

        assertThat(bid.getStatus()).isEqualTo(Xtz73Status.PENDING);
        assertThat(bid.getSale()).isNull();
        assertThat(bid.isInstantPurchase()).isFalse();
    }

    @Test
    void newPurchaseBidCanBeCancelled() {
        Xzj27 bid = register();
        when(orderBidRepository.findById(55L)).thenReturn(Optional.of(bid));

        assertThatCode(() -> service.deleteOrderBid(55L)).doesNotThrowAnyException();
        verify(orderBidRepository).delete(bid);
    }
}
