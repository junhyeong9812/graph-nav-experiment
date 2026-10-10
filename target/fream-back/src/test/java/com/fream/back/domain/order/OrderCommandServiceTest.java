package com.fream.back.domain.order;

import com.fream.back.domain.address.dto.AddressResponseDto;
import com.fream.back.domain.address.service.query.AddressQueryService;
import com.fream.back.domain.order.dto.PayAndShipmentRequestDto;
import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.entity.OrderItem;
import com.fream.back.domain.order.entity.OrderStatus;
import com.fream.back.domain.order.exception.*;
import com.fream.back.domain.order.repository.OrderRepository;
import com.fream.back.domain.order.service.command.OrderCommandService;
import com.fream.back.domain.order.service.command.OrderItemCommandService;
import com.fream.back.domain.order.service.query.OrderBidQueryService;
import com.fream.back.domain.payment.dto.GeneralPaymentRequestDto;
import com.fream.back.domain.payment.dto.PaymentRequestDto;
import com.fream.back.domain.payment.entity.GeneralPayment;
import com.fream.back.domain.payment.entity.Payment;
import com.fream.back.domain.payment.service.command.PaymentCommandService;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleBid;
import com.fream.back.domain.shipment.entity.OrderShipment;
import com.fream.back.domain.shipment.service.command.OrderShipmentCommandService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.service.query.UserQueryService;
import com.fream.back.domain.warehouseStorage.entity.WarehouseStatus;
import com.fream.back.domain.warehouseStorage.entity.WarehouseStorage;
import com.fream.back.domain.warehouseStorage.service.command.WarehouseStorageCommandService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * 주문 명령 서비스의 생성 금액·상태 전이·검증 분기·협력 도메인 호출을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock PaymentCommandService paymentCommandService;
    @Mock OrderShipmentCommandService orderShipmentCommandService;
    @Mock OrderItemCommandService orderItemCommandService;
    @Mock OrderBidQueryService orderBidQueryService;
    @Mock UserQueryService userQueryService;
    @Mock WarehouseStorageCommandService warehouseStorageCommandService;
    @Mock AddressQueryService addressQueryService;
    @Mock EntityManager entityManager;

    @InjectMocks OrderCommandService service;

    private final User buyer = User.builder().id(1L).email("buyer@test.com").build();

    private PayAndShipmentRequestDto request(boolean warehouse) {
        return PayAndShipmentRequestDto.builder()
                .paymentRequest(GeneralPaymentRequestDto.builder().impUid("imp_1").paidAmount(1000).build())
                .receiverName("홍길동").receiverPhone("010-1111-2222").postalCode("12345").address("서울")
                .warehouseStorage(warehouse)
                .build();
    }

    @Test
    void createOrderFromBidBuildsPendingOrderWithBidPriceAsTotal() {
        ProductSize size = ProductSize.builder().id(10L).build();
        OrderItem item = OrderItem.builder().productSize(size).quantity(1).price(150000).build();
        when(orderItemCommandService.createOrderItem(null, size, 150000)).thenReturn(item);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = service.createOrderFromBid(buyer, size, 150000);

        assertThat(order.getUser()).isSameAs(buyer);
        assertThat(order.getTotalAmount()).isEqualTo(150000);
        assertThat(order.getDiscountAmount()).isZero();
        assertThat(order.getUsedPoints()).isZero();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getOrderItems()).containsExactly(item);
        assertThat(item.getOrder()).isSameAs(order);
    }

    @Test
    void createOrderFromBidValidatesInputsBeforeAnyCollaboratorCall() {
        ProductSize size = ProductSize.builder().build();

        assertThatThrownBy(() -> service.createOrderFromBid(null, size, 1000))
                .isInstanceOf(InvalidOrderDataException.class);
        assertThatThrownBy(() -> service.createOrderFromBid(buyer, null, 1000))
                .isInstanceOf(ProductSizeNotFoundException.class);
        assertThatThrownBy(() -> service.createOrderFromBid(buyer, size, 0))
                .isInstanceOf(InvalidBidPriceException.class)
                .hasMessageContaining("현재 가격: 0");

        verifyNoInteractions(orderItemCommandService, orderRepository);
    }

    @Test
    void createOrderFromBidWrapsUnexpectedErrorAsCreationFailed() {
        ProductSize size = ProductSize.builder().build();
        when(orderItemCommandService.createOrderItem(any(), any(), anyInt()))
                .thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.createOrderFromBid(buyer, size, 1000))
                .isInstanceOf(OrderCreationFailedException.class)
                .hasMessageContaining("db down");
    }

    @Test
    void processPaymentAndShipmentForDeliveryEndsInPreparing() {
        Order order = Order.builder().id(100L).user(buyer).status(OrderStatus.PENDING_PAYMENT).build();
        PayAndShipmentRequestDto req = request(false);
        Payment payment = GeneralPayment.builder().paidAmount(1000).build();
        OrderShipment shipment = OrderShipment.builder().build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentCommandService.processPayment(order, buyer, req.getPaymentRequest())).thenReturn(payment);
        when(orderShipmentCommandService.createOrderShipment(order, "홍길동", "010-1111-2222", "12345", "서울"))
                .thenReturn(shipment);
        when(orderBidQueryService.findById(100L)).thenReturn(Optional.empty());

        service.processPaymentAndShipment(100L, "buyer@test.com", req);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
        assertThat(order.getPayment()).isSameAs(payment);
        assertThat(payment.getOrder()).isSameAs(order);
        assertThat(order.getOrderShipment()).isSameAs(shipment);
        verifyNoInteractions(warehouseStorageCommandService);
    }

    @Test
    void processPaymentAndShipmentForWarehouseEndsCompletedAndLooksUpBidByOrderId() {
        Order order = Order.builder().id(100L).user(buyer).status(OrderStatus.PENDING_PAYMENT).build();
        PayAndShipmentRequestDto req = request(true);
        WarehouseStorage storage = WarehouseStorage.builder().build();
        OrderBid bid = OrderBid.builder().status(BidStatus.PENDING).build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentCommandService.processPayment(any(), any(), any()))
                .thenReturn(GeneralPayment.builder().paidAmount(1000).build());
        when(orderShipmentCommandService.createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(OrderShipment.builder().build());
        when(warehouseStorageCommandService.createOrderStorage(order, buyer)).thenReturn(storage);
        // 입찰 조회는 주문 ID 값을 그대로 입찰 ID로 넘긴다
        when(orderBidQueryService.findById(100L)).thenReturn(Optional.of(bid));

        service.processPaymentAndShipment(100L, "buyer@test.com", req);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getWarehouseStorage()).isSameAs(storage);
        assertThat(bid.getStatus()).isEqualTo(BidStatus.MATCHED);
        verify(orderBidQueryService).findById(100L);
        verify(orderBidQueryService, never()).findByOrderId(any());
    }

    @Test
    void processPaymentAndShipmentRejectsOtherUsersOrder() {
        User other = User.builder().id(2L).email("other@test.com").build();
        Order order = Order.builder().id(100L).user(buyer).status(OrderStatus.PENDING_PAYMENT).build();
        when(userQueryService.findByEmail("other@test.com")).thenReturn(other);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.processPaymentAndShipment(100L, "other@test.com", request(false)))
                .isInstanceOf(OrderAccessDeniedException.class);
        verifyNoInteractions(paymentCommandService);
    }

    @Test
    void processPaymentAndShipmentValidationAndFailureWrapping() {
        assertThatThrownBy(() -> service.processPaymentAndShipment(null, "a", request(false)))
                .isInstanceOf(InvalidOrderDataException.class);
        assertThatThrownBy(() -> service.processPaymentAndShipment(1L, " ", request(false)))
                .isInstanceOf(OrderAccessDeniedException.class);
        assertThatThrownBy(() -> service.processPaymentAndShipment(1L, "a", null))
                .isInstanceOf(InvalidPaymentShipmentDataException.class);

        Order order = Order.builder().id(100L).user(buyer).status(OrderStatus.PENDING_PAYMENT).build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        PayAndShipmentRequestDto blankPostal = request(false);
        blankPostal.setPostalCode(" ");
        assertThatThrownBy(() -> service.processPaymentAndShipment(100L, "buyer@test.com", blankPostal))
                .isInstanceOf(InvalidPaymentShipmentDataException.class)
                .hasMessage("우편번호가 없습니다.");

        when(paymentCommandService.processPayment(any(), any(), any())).thenThrow(new RuntimeException("pg"));
        assertThatThrownBy(() -> service.processPaymentAndShipment(100L, "buyer@test.com", request(false)))
                .isInstanceOf(OrderPaymentProcessingFailedException.class)
                .hasMessageContaining("pg");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        verifyNoInteractions(orderShipmentCommandService);
    }

    @Test
    void processPaymentAndShipmentFromNonPendingStatusFailsAsStatusUpdate() {
        Order order = Order.builder().id(100L).user(buyer).status(OrderStatus.COMPLETED).build();
        when(userQueryService.findByEmail("buyer@test.com")).thenReturn(buyer);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(paymentCommandService.processPayment(any(), any(), any()))
                .thenReturn(GeneralPayment.builder().paidAmount(1000).build());
        when(orderShipmentCommandService.createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(OrderShipment.builder().build());

        assertThatThrownBy(() -> service.processPaymentAndShipment(100L, "buyer@test.com", request(false)))
                .isInstanceOf(OrderStatusUpdateFailedException.class);
    }

    @Test
    void createInstantOrderMatchesSaleBidUpdatesWarehouseAndPays() {
        User seller = User.builder().id(9L).build();
        Sale sale = Sale.builder().id(50L).seller(seller).isWarehouseStorage(true).build();
        ProductSize size = ProductSize.builder().id(10L).build();
        SaleBid saleBid = SaleBid.builder().id(70L).bidPrice(210000).productSize(size).sale(sale).build();
        PaymentRequestDto pay = GeneralPaymentRequestDto.builder().impUid("imp").paidAmount(210000).build();
        when(addressQueryService.getAddress("buyer@test.com", 3L))
                .thenReturn(new AddressResponseDto(3L, "수령인", "010-9", "54321", "부산", "상세", true));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(o, "id", 777L);
            return o;
        });
        when(orderItemCommandService.createOrderItem(any(Order.class), eq(size), eq(210000)))
                .thenReturn(OrderItem.builder().price(210000).quantity(1).build());
        when(orderShipmentCommandService.createOrderShipment(any(), eq("수령인"), eq("010-9"), eq("54321"), eq("부산")))
                .thenReturn(OrderShipment.builder().build());

        Order order = service.createInstantOrder(buyer, saleBid, 3L, false, pay);

        assertThat(order.getId()).isEqualTo(777L);
        assertThat(order.getTotalAmount()).isEqualTo(210000);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(pay.getOrderId()).isEqualTo(777L);
        assertThat(saleBid.getOrder()).isSameAs(order);
        assertThat(saleBid.getStatus()).isEqualTo(com.fream.back.domain.sale.entity.BidStatus.MATCHED);
        verify(entityManager).flush();
        verify(warehouseStorageCommandService).updateWarehouseStatus(sale, WarehouseStatus.ASSOCIATED_WITH_ORDER);
        verify(paymentCommandService).processPayment(order, buyer, pay);
    }

    @Test
    void createInstantOrderValidatesInputs() {
        SaleBid saleBid = SaleBid.builder().build();
        PaymentRequestDto pay = GeneralPaymentRequestDto.builder().build();

        assertThatThrownBy(() -> service.createInstantOrder(null, saleBid, 1L, false, pay))
                .isInstanceOf(InvalidOrderDataException.class);
        assertThatThrownBy(() -> service.createInstantOrder(buyer, null, 1L, false, pay))
                .isInstanceOf(SaleBidNotFoundException.class);
        assertThatThrownBy(() -> service.createInstantOrder(buyer, saleBid, null, false, pay))
                .isInstanceOf(InvalidOrderDataException.class);
        assertThatThrownBy(() -> service.createInstantOrder(buyer, saleBid, 1L, false, null))
                .isInstanceOf(InvalidPaymentShipmentDataException.class);
        verifyNoInteractions(addressQueryService, orderRepository);
    }

    @Test
    void deleteOrderDeletesFoundOrderOrThrowsNotFound() {
        Order order = Order.builder().id(5L).build();
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.findById(6L)).thenReturn(Optional.empty());

        service.deleteOrder(5L);
        verify(orderRepository).delete(order);

        assertThatThrownBy(() -> service.deleteOrder(6L)).isInstanceOf(OrderNotFoundException.class);
        verify(orderRepository, never()).delete(isNull());
    }
}
