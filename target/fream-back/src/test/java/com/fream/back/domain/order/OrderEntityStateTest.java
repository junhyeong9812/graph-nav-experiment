package com.fream.back.domain.order;

import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.entity.OrderItem;
import com.fream.back.domain.order.entity.OrderStatus;
import com.fream.back.domain.order.exception.InvalidOrderStatusException;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static com.fream.back.domain.order.entity.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 주문 엔티티의 상태 전이 표와 양방향 연관 설정 동작을 고정한다.
 */
class OrderEntityStateTest {

    @Test
    void orderStatusTransitionTableIsFixed() {
        assertThat(PENDING_PAYMENT.getAllowedNextStates())
                .containsExactlyInAnyOrder(PAYMENT_COMPLETED, COMPLETED, IN_WAREHOUSE);
        assertThat(PAYMENT_COMPLETED.getAllowedNextStates())
                .containsExactlyInAnyOrder(PREPARING, REFUND_REQUESTED, IN_WAREHOUSE);
        assertThat(PREPARING.getAllowedNextStates())
                .containsExactlyInAnyOrder(IN_WAREHOUSE, SHIPMENT_STARTED);
        assertThat(IN_WAREHOUSE.getAllowedNextStates())
                .containsExactlyInAnyOrder(SHIPMENT_STARTED, COMPLETED);
        assertThat(SHIPMENT_STARTED.getAllowedNextStates()).containsExactly(IN_TRANSIT);
        assertThat(IN_TRANSIT.getAllowedNextStates()).containsExactly(COMPLETED);
        assertThat(REFUND_REQUESTED.getAllowedNextStates()).containsExactly(REFUNDED);
        assertThat(COMPLETED.getAllowedNextStates()).isEmpty();
        assertThat(REFUNDED.getAllowedNextStates()).isEmpty();
    }

    @Test
    void canTransitionToMatchesAllowedNextStatesForEveryPair() {
        for (OrderStatus from : OrderStatus.values()) {
            Set<OrderStatus> allowed = from.getAllowedNextStates();
            for (OrderStatus to : OrderStatus.values()) {
                assertThat(from.canTransitionTo(to))
                        .as("%s -> %s", from, to)
                        .isEqualTo(allowed.contains(to));
            }
        }
        // 자기 자신으로의 전이는 어떤 상태에서도 허용되지 않는다
        for (OrderStatus s : EnumSet.allOf(OrderStatus.class)) {
            assertThat(s.canTransitionTo(s)).isFalse();
        }
    }

    @Test
    void updateStatusFromNullAcceptsAnyStatus() {
        Order order = Order.builder().build();
        assertThat(order.getStatus()).isNull();

        order.updateStatus(REFUNDED);

        assertThat(order.getStatus()).isEqualTo(REFUNDED);
    }

    @Test
    void updateStatusFollowsWarehouseChainAndRejectsInvalidTransition() {
        Order order = Order.builder().status(PENDING_PAYMENT).build();

        order.updateStatus(PAYMENT_COMPLETED);
        order.updateStatus(PREPARING);
        order.updateStatus(IN_WAREHOUSE);
        order.updateStatus(COMPLETED);
        assertThat(order.getStatus()).isEqualTo(COMPLETED);

        assertThatThrownBy(() -> order.updateStatus(REFUND_REQUESTED))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessage("상태 전환이 허용되지 않습니다: COMPLETED -> REFUND_REQUESTED");
        assertThat(order.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    void addOrderItemSetsBackReference() {
        Order order = Order.builder().build();
        OrderItem item = OrderItem.builder().quantity(1).price(1000).build();

        order.addOrderItem(item);

        assertThat(order.getOrderItems()).containsExactly(item);
        assertThat(item.getOrder()).isSameAs(order);
    }

    @Test
    void orderBidAssignOrderLinksBothSidesAndMarksMatched() {
        Order order = Order.builder().build();
        OrderBid bid = OrderBid.builder().bidPrice(1000).build();
        assertThat(bid.getStatus()).isEqualTo(BidStatus.PENDING);
        assertThat(bid.isInstantPurchase()).isFalse();

        bid.assignOrder(order);

        assertThat(bid.getOrder()).isSameAs(order);
        assertThat(order.getOrderBid()).isSameAs(bid);
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
    }

    @Test
    void orderBidReassignOrderDetachesPreviousOrder() {
        Order first = Order.builder().build();
        Order second = Order.builder().build();
        OrderBid bid = OrderBid.builder().build();

        bid.assignOrder(first);
        bid.assignOrder(second);

        assertThat(bid.getOrder()).isSameAs(second);
        assertThat(second.getOrderBid()).isSameAs(bid);
        assertThat(first.getOrderBid()).isNull();
    }

    @Test
    void orderBidAssignNullOrderKeepsStatusAndAssignSaleMarksMatched() {
        OrderBid bid = OrderBid.builder().build();

        bid.assignOrder(null);
        assertThat(bid.getStatus()).isEqualTo(BidStatus.PENDING);

        bid.assignSale(null);
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);

        bid.markAsInstantPurchase();
        assertThat(bid.isInstantPurchase()).isTrue();
    }
}
