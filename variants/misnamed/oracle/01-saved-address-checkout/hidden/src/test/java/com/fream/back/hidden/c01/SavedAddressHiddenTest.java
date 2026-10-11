package com.fream.back.hidden.c01;

import com.fream.back.domain.shipment.dto.ShipmentCreateDto;
import com.fream.back.domain.shipment.dto.ShipmentResponseDto;
import com.fream.back.domain.shipment.dto.ShipmentUpdateDto;
import com.fream.back.domain.shipment.entity.Shipment;
import com.fream.back.domain.shipment.exception.ShipmentNotFoundException;
import com.fream.back.domain.shipment.exception.ShipmentProductNotFoundException;
import com.fream.back.domain.shipment.repository.ShipmentRepository;
import com.fream.back.domain.shipment.service.command.ShipmentCommandService;
import com.fream.back.domain.shipment.service.query.ShipmentQueryService;
import com.fream.back.domain.faq.entity.Faq;
import com.fream.back.domain.faq.entity.FaqItem;
import com.fream.back.domain.faq.repository.FaqRepository;
import com.fream.back.domain.faq.service.command.FaqCommandService;
import com.fream.back.domain.faq.service.command.FaqItemCommandService;
import com.fream.back.domain.faq.service.query.FaqBidQueryService;
import com.fream.back.domain.notification.dto.GeneralNotificationRequestDto;
import com.fream.back.domain.notification.service.command.NotificationCommandService;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleBid;
import com.fream.back.domain.address.entity.FaqAddress;
import com.fream.back.domain.address.service.command.FaqAddressCommandService;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.service.query.ProductQueryService;
import com.fream.back.domain.notice.service.command.NoticeCommandService;
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
@Import({QueryDslConfig.class, PersonalDataEncryptionUtil.class, ShipmentCommandService.class, ShipmentQueryService.class})
class SavedAddressHiddenTest {

    @Autowired TestEntityManager em;
    @Autowired ShipmentRepository addressRepository;
    @Autowired FaqRepository orderRepository;
    @Autowired EntityManager entityManager;
    @Autowired ShipmentCommandService commandService;
    @Autowired ShipmentQueryService queryService;

    private Product me;

    private static ShipmentCreateDto form(String name, String phone, String zip, String addr, String detail, boolean isDefault) {
        ShipmentCreateDto dto = new ShipmentCreateDto();
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
                .map(ShipmentResponseDto::getId).findFirst().orElseThrow();
    }

    @BeforeEach
    void setUp() {
        me = em.persist(Product.builder().email("me@fream.com").password("pw").referralCode("REFME")
                .phoneNumber("01000000000").build());
        em.persist(Product.builder().email("other@fream.com").password("pw").referralCode("REFOT")
                .phoneNumber("01000000001").build());
        newRequest();
        commandService.createAddress("me@fream.com", form("홍길동", "01012345678", "06236", "서울시 강남구 테헤란로 1", "101동 1001호", true));
        commandService.createAddress("me@fream.com", form("김철수", "0311234567", "13494", "경기도 성남시 분당구 판교역로 2", null, false));
        commandService.createAddress("other@fream.com", form("이영희", "01055556666", "48058", "부산시 해운대구 우동 3", "B동", true));
        newRequest();
    }

    @Test
    void storedRowsAreNotPlainText() {
        List<Shipment> rows = addressRepository.findAll();
        assertThat(rows).hasSize(3);
        assertThat(rows).extracting(Shipment::getRecipientName).doesNotContain("홍길동", "김철수", "이영희");
        assertThat(rows).extracting(Shipment::getPhoneNumber).doesNotContain("01012345678", "0311234567", "01055556666");
        assertThat(rows).extracting(Shipment::getAddress).doesNotContain("서울시 강남구 테헤란로 1");
        assertThat(rows).extracting(Shipment::getDetailedAddress).doesNotContain("101동 1001호", "B동");
    }

    @Test
    void listReturnsOwnAddressesAsEntered() {
        List<ShipmentResponseDto> list = queryService.getAddresses("me@fream.com");

        assertThat(list).hasSize(2);
        ShipmentResponseDto hong = list.stream().filter(a -> "홍길동".equals(a.getRecipientName())).findFirst().orElseThrow();
        assertThat(hong.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(hong.getZipCode()).isEqualTo("06236");
        assertThat(hong.getAddress()).isEqualTo("서울시 강남구 테헤란로 1");
        assertThat(hong.getDetailedAddress()).isEqualTo("101동 1001호");
        assertThat(hong.getIsDefault()).isTrue();

        ShipmentResponseDto kim = list.stream().filter(a -> "김철수".equals(a.getRecipientName())).findFirst().orElseThrow();
        assertThat(kim.getPhoneNumber()).isEqualTo("0311234567");
        assertThat(kim.getZipCode()).isEqualTo("13494");
        assertThat(kim.getAddress()).isEqualTo("경기도 성남시 분당구 판교역로 2");
        assertThat(kim.getDetailedAddress()).isNull();
        assertThat(kim.getIsDefault()).isFalse();
    }

    @Test
    void singleLookupIsScopedToOwner() {
        Long hongId = idOf("홍길동");
        ShipmentResponseDto dto = queryService.getAddress("me@fream.com", hongId);
        assertThat(dto.getRecipientName()).isEqualTo("홍길동");
        assertThat(dto.getAddress()).isEqualTo("서울시 강남구 테헤란로 1");

        Long othersId = addressRepository.findAll().stream()
                .filter(a -> "other@fream.com".equals(a.getUser().getEmail()))
                .map(Shipment::getId).findFirst().orElseThrow();
        assertThatThrownBy(() -> queryService.getAddress("me@fream.com", othersId))
                .isInstanceOf(ShipmentNotFoundException.class);
        assertThatThrownBy(() -> queryService.getAddresses("ghost@fream.com"))
                .isInstanceOf(ShipmentProductNotFoundException.class);
    }

    @Test
    void searchByRecipientAndPhoneFindsAsEntered() {
        assertThat(queryService.searchAddressesByRecipientName("me@fream.com", "김철수"))
                .extracting(ShipmentResponseDto::getZipCode).containsExactly("13494");
        assertThat(queryService.searchAddressesByPhoneNumber("me@fream.com", "01012345678"))
                .extracting(ShipmentResponseDto::getRecipientName).containsExactly("홍길동");
        // 다른 회원의 배송지는 검색되지 않는다
        assertThat(queryService.searchAddressesByRecipientName("me@fream.com", "이영희")).isEmpty();
    }

    @Test
    void updatedAddressIsReadBackAsEntered() {
        Long kimId = idOf("김철수");
        ShipmentUpdateDto update = new ShipmentUpdateDto();
        update.setAddressId(kimId);
        update.setAddress("경기도 성남시 수정구 4");
        update.setDetailedAddress("2층");
        commandService.updateAddress("me@fream.com", update);
        newRequest();

        ShipmentResponseDto dto = queryService.getAddress("me@fream.com", kimId);
        assertThat(dto.getRecipientName()).isEqualTo("김철수");
        assertThat(dto.getAddress()).isEqualTo("경기도 성남시 수정구 4");
        assertThat(dto.getDetailedAddress()).isEqualTo("2층");
    }

    @Test
    void instantPurchaseShipsToChosenSavedAddress() {
        Long kimId = idOf("김철수");
        Product buyer = em.find(Product.class, me.getId());

        FaqItemCommandService orderItems = mock(FaqItemCommandService.class);
        FaqAddressCommandService shipments = mock(FaqAddressCommandService.class);
        FaqCommandService orderService = new FaqCommandService(
                orderRepository, mock(NotificationCommandService.class), shipments, orderItems,
                mock(FaqBidQueryService.class), mock(ProductQueryService.class),
                mock(NoticeCommandService.class), queryService, entityManager);

        UserSize size = UserSize.builder().id(11L).build();
        StyleBid saleBid = StyleBid.builder().id(71L).bidPrice(99000).productSize(size)
                .sale(Style.builder().id(51L).isWarehouseStorage(false).build()).build();
        when(orderItems.createOrderItem(any(Faq.class), any(), any(Integer.class)))
                .thenReturn(FaqItem.builder().price(99000).quantity(1).build());
        when(shipments.createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(FaqAddress.builder().build());

        Faq order = orderService.createInstantOrder(buyer, saleBid, kimId, false,
                GeneralNotificationRequestDto.builder().impUid("imp_h").paidAmount(99000).userEmail("me@fream.com").build());

        assertThat(order.getId()).isNotNull();
        verify(shipments).createOrderShipment(order, "김철수", "0311234567", "13494", "경기도 성남시 분당구 판교역로 2");
    }
}
