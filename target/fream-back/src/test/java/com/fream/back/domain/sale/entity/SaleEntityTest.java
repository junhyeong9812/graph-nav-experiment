package com.fream.back.domain.sale.entity;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.fream.back.domain.sale.entity.SaleStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sale·SaleBid·SaleStatus 엔티티 수준의 현재 동작을 고정한다.
 */
class SaleEntityTest {

    @Test
    void saleStatusTransitionMatrix() {
        Map<SaleStatus, Set<SaleStatus>> allowed = Map.of(
                PENDING_SHIPMENT, EnumSet.of(IN_TRANSIT),
                IN_TRANSIT, EnumSet.of(IN_INSPECTION),
                IN_INSPECTION, EnumSet.of(FAILED_INSPECTION, IN_STORAGE),
                IN_STORAGE, EnumSet.of(ON_AUCTION, AUCTION_EXPIRED, SELLING),
                ON_AUCTION, EnumSet.of(SOLD, AUCTION_EXPIRED),
                SELLING, EnumSet.of(SOLD, AUCTION_EXPIRED, ON_AUCTION),
                SOLD, EnumSet.noneOf(SaleStatus.class),
                AUCTION_EXPIRED, EnumSet.noneOf(SaleStatus.class),
                FAILED_INSPECTION, EnumSet.noneOf(SaleStatus.class)
        );

        for (SaleStatus from : SaleStatus.values()) {
            for (SaleStatus to : SaleStatus.values()) {
                assertThat(from.canTransitionTo(to))
                        .as("%s -> %s", from, to)
                        .isEqualTo(allowed.get(from).contains(to));
            }
        }
    }

    @Test
    void saleBidBuilderDefaults() {
        SaleBid bid = SaleBid.builder().bidPrice(1000).build();

        assertThat(bid.getStatus()).isEqualTo(BidStatus.PENDING);
        assertThat(bid.isInstantSale()).isFalse();
    }

    @Test
    void saleBidAssignSaleSetsMatched() {
        SaleBid bid = SaleBid.builder().status(BidStatus.PENDING).build();
        Sale sale = Sale.builder().status(PENDING_SHIPMENT).build();

        bid.assignSale(sale);

        assertThat(bid.getSale()).isSameAs(sale);
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
    }

    @Test
    void saleBidAssignOrderNullStillSetsMatched() {
        SaleBid bid = SaleBid.builder().status(BidStatus.CANCELLED).build();

        bid.assignOrder(null);

        assertThat(bid.getOrder()).isNull();
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
    }

    @Test
    void saleAssignSaleBidLinksBothSidesAndMatchesBid() {
        Sale sale = Sale.builder().status(PENDING_SHIPMENT).build();
        SaleBid bid = SaleBid.builder().status(BidStatus.PENDING).build();

        sale.assignSaleBid(bid);

        assertThat(sale.getSaleBid()).isSameAs(bid);
        assertThat(bid.getSale()).isSameAs(sale);
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
    }

    @Test
    void saleAssignSaleBankAccountLinksBothSides() {
        Sale sale = Sale.builder().build();
        SaleBankAccount account = new SaleBankAccount("국민", "123", "홍길동", null);

        sale.assignSaleBankAccount(account);

        assertThat(sale.getSaleBankAccount()).isSameAs(account);
        assertThat(account.getSale()).isSameAs(sale);
    }

    @Test
    void saleUpdateStatusDoesNotCheckTransitionRule() {
        Sale sale = Sale.builder().status(SOLD).build();

        sale.updateStatus(PENDING_SHIPMENT);

        assertThat(sale.getStatus()).isEqualTo(PENDING_SHIPMENT);
    }
}
