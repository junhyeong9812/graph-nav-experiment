package com.fream.back.hidden.c01;

import com.fream.back.domain.address.dto.AddressCreateDto;
import com.fream.back.domain.address.dto.AddressResponseDto;
import com.fream.back.domain.address.dto.AddressUpdateDto;
import com.fream.back.domain.address.entity.Address;
import com.fream.back.domain.address.exception.AddressNotFoundException;
import com.fream.back.domain.address.exception.AddressUserNotFoundException;
import com.fream.back.domain.address.repository.AddressRepository;
import com.fream.back.domain.address.service.command.AddressCommandService;
import com.fream.back.domain.address.service.query.AddressQueryService;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderItem;
import com.fream.back.domain.order.repository.OrderRepository;
import com.fream.back.domain.order.service.command.OrderCommandService;
import com.fream.back.domain.order.service.command.OrderItemCommandService;
import com.fream.back.domain.order.service.query.OrderBidQueryService;
import com.fream.back.domain.payment.dto.GeneralPaymentRequestDto;
import com.fream.back.domain.payment.service.command.PaymentCommandService;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleBid;
import com.fream.back.domain.shipment.entity.OrderShipment;
import com.fream.back.domain.shipment.service.command.OrderShipmentCommandService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.service.query.UserQueryService;
import com.fream.back.domain.warehouseStorage.service.command.WarehouseStorageCommandService;
import com.fream.back.global.config.QueryDslConfig;
import com.fream.back.global.utils.PersonalDataEncryptionUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 배송지 등록 → 조회(목록·단건·검색) → 수정 → 즉시 구매까지, 실제 서비스·실제 DB로 확인한다.
 * 저장 형태는 원문이 아니어야 한다.
 */
@DataJpaTest(properties = {
        "personal-data.encryption.secret-key=0123456789abcdef",
        "personal-data.encryption.iv=fedcba9876543210"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({QueryDslConfig.class, PersonalDataEncryptionUtil.class, AddressCommandService.class, AddressQueryService.class})
class SavedAddressHiddenTest {

    @Autowired TestEntityManager em;
    @Autowired AddressRepository addressRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired EntityManager entityManager;
    @Autowired AddressCommandService commandService;
    @Autowired AddressQueryService queryService;

    private User me;

    private static AddressCreateDto form(String name, String phone, String zip, String addr, String detail, boolean isDefault) {
        AddressCreateDto dto = new AddressCreateDto();
        dto.setRecipientName(name);
        dto.setPhoneNumber(phone);
        dto.setZipCode(zip);
        dto.setAddress(addr);
        dto.setDetailedAddress(detail);
        dto.setIsDefault(isDefault);
        return dto;
    }

    private void newRequest() {
        em.flush();
        em.clear();
    }

    private Long idOf(String plainName) {
        return queryService.getAddresses("me@fream.com").stream()
                .filter(a -> plainName.equals(a.getRecipientName()))
                .map(AddressResponseDto::getId).findFirst().orElseThrow();
    }

    @BeforeEach
    void setUp() {
        me = em.persist(User.builder().email("me@fream.com").password("pw").referralCode("REFME")
                .phoneNumber("01000000000").build());
        em.persist(User.builder().email("other@fream.com").password("pw").referralCode("REFOT")
                .phoneNumber("01000000001").build());
        newRequest();
        commandService.createAddress("me@fream.com", form("홍길동", "01012345678", "06236", "서울시 강남구 테헤란로 1", "101동 1001호", true));
        commandService.createAddress("me@fream.com", form("김철수", "0311234567", "13494", "경기도 성남시 분당구 판교역로 2", null, false));
        commandService.createAddress("other@fream.com", form("이영희", "01055556666", "48058", "부산시 해운대구 우동 3", "B동", true));
        newRequest();
    }

    @Test
    void storedRowsAreNotPlainText() {
        List<Address> rows = addressRepository.findAll();
        assertThat(rows).hasSize(3);
        assertThat(rows).extracting(Address::getRecipientName).doesNotContain("홍길동", "김철수", "이영희");
        assertThat(rows).extracting(Address::getPhoneNumber).doesNotContain("01012345678", "0311234567", "01055556666");
        assertThat(rows).extracting(Address::getAddress).doesNotContain("서울시 강남구 테헤란로 1");
        assertThat(rows).extracting(Address::getDetailedAddress).doesNotContain("101동 1001호", "B동");
    }

    @Test
    void listReturnsOwnAddressesAsEntered() {
        List<AddressResponseDto> list = queryService.getAddresses("me@fream.com");

        assertThat(list).hasSize(2);
        AddressResponseDto hong = list.stream().filter(a -> "홍길동".equals(a.getRecipientName())).findFirst().orElseThrow();
        assertThat(hong.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(hong.getZipCode()).isEqualTo("06236");
        assertThat(hong.getAddress()).isEqualTo("서울시 강남구 테헤란로 1");
        assertThat(hong.getDetailedAddress()).isEqualTo("101동 1001호");
        assertThat(hong.getIsDefault()).isTrue();

        AddressResponseDto kim = list.stream().filter(a -> "김철수".equals(a.getRecipientName())).findFirst().orElseThrow();
        assertThat(kim.getPhoneNumber()).isEqualTo("0311234567");
        assertThat(kim.getZipCode()).isEqualTo("13494");
        assertThat(kim.getAddress()).isEqualTo("경기도 성남시 분당구 판교역로 2");
        assertThat(kim.getDetailedAddress()).isNull();
        assertThat(kim.getIsDefault()).isFalse();
    }

    @Test
    void singleLookupIsScopedToOwner() {
        Long hongId = idOf("홍길동");
        AddressResponseDto dto = queryService.getAddress("me@fream.com", hongId);
        assertThat(dto.getRecipientName()).isEqualTo("홍길동");
        assertThat(dto.getAddress()).isEqualTo("서울시 강남구 테헤란로 1");

        Long othersId = addressRepository.findAll().stream()
                .filter(a -> "other@fream.com".equals(a.getUser().getEmail()))
                .map(Address::getId).findFirst().orElseThrow();
        assertThatThrownBy(() -> queryService.getAddress("me@fream.com", othersId))
                .isInstanceOf(AddressNotFoundException.class);
        assertThatThrownBy(() -> queryService.getAddresses("ghost@fream.com"))
                .isInstanceOf(AddressUserNotFoundException.class);
    }

    @Test
    void searchByRecipientAndPhoneFindsAsEntered() {
        assertThat(queryService.searchAddressesByRecipientName("me@fream.com", "김철수"))
                .extracting(AddressResponseDto::getZipCode).containsExactly("13494");
        assertThat(queryService.searchAddressesByPhoneNumber("me@fream.com", "01012345678"))
                .extracting(AddressResponseDto::getRecipientName).containsExactly("홍길동");
        // 다른 회원의 배송지는 검색되지 않는다
        assertThat(queryService.searchAddressesByRecipientName("me@fream.com", "이영희")).isEmpty();
    }

    @Test
    void updatedAddressIsReadBackAsEntered() {
        Long kimId = idOf("김철수");
        AddressUpdateDto update = new AddressUpdateDto();
        update.setAddressId(kimId);
        update.setAddress("경기도 성남시 수정구 4");
        update.setDetailedAddress("2층");
        commandService.updateAddress("me@fream.com", update);
        newRequest();

        AddressResponseDto dto = queryService.getAddress("me@fream.com", kimId);
        assertThat(dto.getRecipientName()).isEqualTo("김철수");
        assertThat(dto.getAddress()).isEqualTo("경기도 성남시 수정구 4");
        assertThat(dto.getDetailedAddress()).isEqualTo("2층");
    }

    @Test
    void instantPurchaseShipsToChosenSavedAddress() {
        Long kimId = idOf("김철수");
        User buyer = em.find(User.class, me.getId());

        OrderItemCommandService orderItems = mock(OrderItemCommandService.class);
        OrderShipmentCommandService shipments = mock(OrderShipmentCommandService.class);
        OrderCommandService orderService = new OrderCommandService(
                orderRepository, mock(PaymentCommandService.class), shipments, orderItems,
                mock(OrderBidQueryService.class), mock(UserQueryService.class),
                mock(WarehouseStorageCommandService.class), queryService, entityManager);

        ProductSize size = ProductSize.builder().id(11L).build();
        SaleBid saleBid = SaleBid.builder().id(71L).bidPrice(99000).productSize(size)
                .sale(Sale.builder().id(51L).isWarehouseStorage(false).build()).build();
        when(orderItems.createOrderItem(any(Order.class), any(), any(Integer.class)))
                .thenReturn(OrderItem.builder().price(99000).quantity(1).build());
        when(shipments.createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(OrderShipment.builder().build());

        Order order = orderService.createInstantOrder(buyer, saleBid, kimId, false,
                GeneralPaymentRequestDto.builder().impUid("imp_h").paidAmount(99000).userEmail("me@fream.com").build());

        assertThat(order.getId()).isNotNull();
        verify(shipments).createOrderShipment(order, "김철수", "0311234567", "13494", "경기도 성남시 분당구 판교역로 2");
    }
}
