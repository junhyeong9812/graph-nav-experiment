package com.fream.back.domain.sale.service;

import com.fream.back.domain.notification.entity.NotificationCategory;
import com.fream.back.domain.notification.entity.NotificationType;
import com.fream.back.domain.notification.service.command.NotificationCommandService;
import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.service.query.OrderBidQueryService;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleBankAccount;
import com.fream.back.domain.sale.entity.SaleStatus;
import com.fream.back.domain.sale.repository.SaleRepository;
import com.fream.back.domain.sale.service.command.SaleBankAccountCommandService;
import com.fream.back.domain.sale.service.command.SaleCommandService;
import com.fream.back.domain.shipment.entity.SellerShipment;
import com.fream.back.domain.shipment.service.command.SellerShipmentCommandService;
import com.fream.back.domain.user.dto.BankAccount.BankAccountInfoDto;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.service.bankaccount.BankAccountQueryService;
import com.fream.back.domain.user.service.query.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SaleCommandService의 판매 생성·상태 전환·삭제 흐름과 다른 도메인 호출을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class SaleCommandServiceTest {

    @Mock SaleRepository saleRepository;
    @Mock OrderBidQueryService orderBidQueryService;
    @Mock NotificationCommandService notificationCommandService;
    @Mock UserQueryService userQueryService;
    @Mock SellerShipmentCommandService sellerShipmentCommandService;
    @Mock BankAccountQueryService bankAccountQueryService;
    @Mock SaleBankAccountCommandService saleBankAccountCommandService;

    @InjectMocks SaleCommandService saleCommandService;

    private User seller;
    private User buyer;
    private ProductSize productSize;

    @BeforeEach
    void setUp() {
        seller = User.builder().id(1L).email("seller@test.com").build();
        buyer = User.builder().id(2L).email("buyer@test.com").build();
        productSize = ProductSize.builder().id(7L).size("260").build();
    }

    private void stubBankAccountAndSave() {
        when(bankAccountQueryService.getBankAccount("seller@test.com"))
                .thenReturn(new BankAccountInfoDto("국민", "111-222", "판매자"));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
        when(saleBankAccountCommandService.createSaleBankAccount(anyString(), anyString(), anyString(), any(Sale.class)))
                .thenAnswer(inv -> new SaleBankAccount(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2), inv.getArgument(3)));
    }

    @Test
    void createSaleBuildsPendingShipmentSaleWithBankAccountAndNotifiesSeller() {
        stubBankAccountAndSave();

        Sale sale = saleCommandService.createSale(seller, productSize, "서울", "12345", "010-0000-0000", true);

        assertThat(sale.getSeller()).isSameAs(seller);
        assertThat(sale.getProductSize()).isSameAs(productSize);
        assertThat(sale.getReturnAddress()).isEqualTo("서울");
        assertThat(sale.getPostalCode()).isEqualTo("12345");
        assertThat(sale.getReceiverPhone()).isEqualTo("010-0000-0000");
        assertThat(sale.isWarehouseStorage()).isTrue();
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.PENDING_SHIPMENT);
        assertThat(sale.getSaleBankAccount().getBankName()).isEqualTo("국민");
        assertThat(sale.getSaleBankAccount().getAccountNumber()).isEqualTo("111-222");
        assertThat(sale.getSaleBankAccount().getAccountHolder()).isEqualTo("판매자");
        assertThat(sale.getSaleBankAccount().getSale()).isSameAs(sale);

        verify(notificationCommandService).createNotification(
                1L, NotificationCategory.SHOPPING, NotificationType.BID, "판매 입찰이 등록되었습니다.");
    }

    @Test
    void createSaleReadsBankAccountBeforeSaving() {
        stubBankAccountAndSave();

        saleCommandService.createSale(seller, productSize, "a", "b", "c", false);

        InOrder order = inOrder(bankAccountQueryService, saleRepository, saleBankAccountCommandService, notificationCommandService);
        order.verify(bankAccountQueryService).getBankAccount("seller@test.com");
        order.verify(saleRepository).save(any(Sale.class));
        order.verify(saleBankAccountCommandService).createSaleBankAccount(eq("국민"), eq("111-222"), eq("판매자"), any(Sale.class));
        order.verify(notificationCommandService).createNotification(anyLong(), any(), any(), anyString());
    }

    @Test
    void createInstantSaleLinksOrderBidAndNotifiesBuyer() {
        OrderBid orderBid = OrderBid.builder().id(5L).user(buyer).productSize(productSize).bidPrice(150000).build();
        when(orderBidQueryService.findById(5L)).thenReturn(Optional.of(orderBid));
        when(userQueryService.findByEmail("seller@test.com")).thenReturn(seller);
        stubBankAccountAndSave();

        Sale sale = saleCommandService.createInstantSale(5L, "seller@test.com", "주소", "00000", "010");

        assertThat(sale.getSeller()).isSameAs(seller);
        assertThat(sale.getProductSize()).isSameAs(productSize);
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.PENDING_SHIPMENT);
        assertThat(sale.isWarehouseStorage()).isFalse();
        assertThat(sale.getSaleBankAccount()).isNotNull();
        assertThat(orderBid.getSale()).isSameAs(sale);
        assertThat(orderBid.getStatus()).isEqualTo(BidStatus.MATCHED);
        verify(notificationCommandService).createNotification(
                2L, NotificationCategory.SHOPPING, NotificationType.BID, "구매 입찰에 판매자가 등록되었습니다. 확인해보세요!");
    }

    @Test
    void createInstantSaleFailsWhenOrderBidMissing() {
        when(orderBidQueryService.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleCommandService.createInstantSale(99L, "seller@test.com", "a", "b", "c"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 OrderBid를 찾을 수 없습니다.");
        verifyNoInteractions(saleRepository, notificationCommandService, saleBankAccountCommandService);
    }

    @Test
    void updateSaleStatusAppliesAllowedTransition() {
        Sale sale = Sale.builder().id(3L).status(SaleStatus.IN_STORAGE).build();
        when(saleRepository.findById(3L)).thenReturn(Optional.of(sale));

        saleCommandService.updateSaleStatus(3L, SaleStatus.SELLING);

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.SELLING);
    }

    @Test
    void updateSaleStatusRejectsDisallowedTransition() {
        Sale sale = Sale.builder().id(3L).status(SaleStatus.PENDING_SHIPMENT).build();
        when(saleRepository.findById(3L)).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleCommandService.updateSaleStatus(3L, SaleStatus.SOLD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Sale 상태를 SOLD로 전환할 수 없습니다.");
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.PENDING_SHIPMENT);
    }

    @Test
    void updateSaleStatusFailsWhenSaleMissing() {
        when(saleRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleCommandService.updateSaleStatus(4L, SaleStatus.IN_TRANSIT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 Sale을 찾을 수 없습니다: 4");
    }

    @Test
    void createSellerShipmentDelegatesThenMovesSaleToInTransit() {
        Sale sale = Sale.builder().id(3L).status(SaleStatus.PENDING_SHIPMENT).build();
        SellerShipment shipment = SellerShipment.builder().id(11L).build();
        when(sellerShipmentCommandService.createSellerShipment(3L, "CJ", "T-1")).thenReturn(shipment);
        when(saleRepository.findById(3L)).thenReturn(Optional.of(sale));

        SellerShipment result = saleCommandService.createSellerShipment(3L, "CJ", "T-1");

        assertThat(result).isSameAs(shipment);
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.IN_TRANSIT);
    }

    @Test
    void createSellerShipmentFailsWhenSaleNotPendingShipment() {
        Sale sale = Sale.builder().id(3L).status(SaleStatus.IN_TRANSIT).build();
        when(sellerShipmentCommandService.createSellerShipment(3L, "CJ", "T-1"))
                .thenReturn(SellerShipment.builder().build());
        when(saleRepository.findById(3L)).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleCommandService.createSellerShipment(3L, "CJ", "T-1"))
                .isInstanceOf(IllegalStateException.class);
        verify(sellerShipmentCommandService).createSellerShipment(3L, "CJ", "T-1");
    }

    @Test
    void deleteSaleDeletesFoundSale() {
        Sale sale = Sale.builder().id(3L).build();
        when(saleRepository.findById(3L)).thenReturn(Optional.of(sale));

        saleCommandService.deleteSale(3L);

        verify(saleRepository).delete(sale);
    }

    @Test
    void deleteSaleFailsWhenMissing() {
        when(saleRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleCommandService.deleteSale(3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 Sale을 찾을 수 없습니다: 3");
        verify(saleRepository, never()).delete(any());
    }
}
