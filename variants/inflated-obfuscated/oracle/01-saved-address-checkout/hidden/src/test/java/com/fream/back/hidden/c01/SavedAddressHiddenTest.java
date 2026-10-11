package com.fream.back.hidden.c01;

import com.fream.back.domain.m53.dto.Xms98CreateDto;
import com.fream.back.domain.m53.dto.Xms98ResponseDto;
import com.fream.back.domain.m53.dto.Xms98UpdateDto;
import com.fream.back.domain.m53.entity.Xms98;
import com.fream.back.domain.m53.exception.Xpp56Exception;
import com.fream.back.domain.m53.exception.Xkk16Exception;
import com.fream.back.domain.m53.repository.Xms98Repository;
import com.fream.back.domain.m53.service.command.Xms98CommandService;
import com.fream.back.domain.m53.service.query.Xms98QueryService;
import com.fream.back.domain.m99.entity.Xhq12;
import com.fream.back.domain.m99.entity.Xnd96;
import com.fream.back.domain.m99.repository.Xhq12Repository;
import com.fream.back.domain.m99.service.command.Xhq12CommandService;
import com.fream.back.domain.m99.service.command.Xnd96CommandService;
import com.fream.back.domain.m99.service.query.Xtw92QueryService;
import com.fream.back.domain.m64.dto.Xgg74RequestDto;
import com.fream.back.domain.m64.service.command.Xjt45CommandService;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m85.entity.Xpj42;
import com.fream.back.domain.m85.entity.Xzt63;
import com.fream.back.domain.m45.entity.Xfr60;
import com.fream.back.domain.m45.service.command.Xfr60CommandService;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.domain.m22.service.query.Xjm45QueryService;
import com.fream.back.domain.m63.service.command.Xms75CommandService;
import com.fream.back.global.config.Xzj77Config;
import com.fream.back.global.utils.Xhr91Util;
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
@Import({Xzj77Config.class, Xhr91Util.class, Xms98CommandService.class, Xms98QueryService.class})
class SavedAddressHiddenTest {

    @Autowired TestEntityManager em;
    @Autowired Xms98Repository addressRepository;
    @Autowired Xhq12Repository orderRepository;
    @Autowired EntityManager entityManager;
    @Autowired Xms98CommandService commandService;
    @Autowired Xms98QueryService queryService;

    private Xjm45 me;

    private static Xms98CreateDto form(String name, String phone, String zip, String addr, String detail, boolean isDefault) {
        Xms98CreateDto dto = new Xms98CreateDto();
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
                .map(Xms98ResponseDto::getId).findFirst().orElseThrow();
    }

    @BeforeEach
    void setUp() {
        me = em.persist(Xjm45.builder().email("me@fream.com").password("pw").referralCode("REFME")
                .phoneNumber("01000000000").build());
        em.persist(Xjm45.builder().email("other@fream.com").password("pw").referralCode("REFOT")
                .phoneNumber("01000000001").build());
        newRequest();
        commandService.createAddress("me@fream.com", form("홍길동", "01012345678", "06236", "서울시 강남구 테헤란로 1", "101동 1001호", true));
        commandService.createAddress("me@fream.com", form("김철수", "0311234567", "13494", "경기도 성남시 분당구 판교역로 2", null, false));
        commandService.createAddress("other@fream.com", form("이영희", "01055556666", "48058", "부산시 해운대구 우동 3", "B동", true));
        newRequest();
    }

    @Test
    void storedRowsAreNotPlainText() {
        List<Xms98> rows = addressRepository.findAll();
        assertThat(rows).hasSize(3);
        assertThat(rows).extracting(Xms98::getRecipientName).doesNotContain("홍길동", "김철수", "이영희");
        assertThat(rows).extracting(Xms98::getPhoneNumber).doesNotContain("01012345678", "0311234567", "01055556666");
        assertThat(rows).extracting(Xms98::getAddress).doesNotContain("서울시 강남구 테헤란로 1");
        assertThat(rows).extracting(Xms98::getDetailedAddress).doesNotContain("101동 1001호", "B동");
    }

    @Test
    void listReturnsOwnAddressesAsEntered() {
        List<Xms98ResponseDto> list = queryService.getAddresses("me@fream.com");

        assertThat(list).hasSize(2);
        Xms98ResponseDto hong = list.stream().filter(a -> "홍길동".equals(a.getRecipientName())).findFirst().orElseThrow();
        assertThat(hong.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(hong.getZipCode()).isEqualTo("06236");
        assertThat(hong.getAddress()).isEqualTo("서울시 강남구 테헤란로 1");
        assertThat(hong.getDetailedAddress()).isEqualTo("101동 1001호");
        assertThat(hong.getIsDefault()).isTrue();

        Xms98ResponseDto kim = list.stream().filter(a -> "김철수".equals(a.getRecipientName())).findFirst().orElseThrow();
        assertThat(kim.getPhoneNumber()).isEqualTo("0311234567");
        assertThat(kim.getZipCode()).isEqualTo("13494");
        assertThat(kim.getAddress()).isEqualTo("경기도 성남시 분당구 판교역로 2");
        assertThat(kim.getDetailedAddress()).isNull();
        assertThat(kim.getIsDefault()).isFalse();
    }

    @Test
    void singleLookupIsScopedToOwner() {
        Long hongId = idOf("홍길동");
        Xms98ResponseDto dto = queryService.getAddress("me@fream.com", hongId);
        assertThat(dto.getRecipientName()).isEqualTo("홍길동");
        assertThat(dto.getAddress()).isEqualTo("서울시 강남구 테헤란로 1");

        Long othersId = addressRepository.findAll().stream()
                .filter(a -> "other@fream.com".equals(a.getUser().getEmail()))
                .map(Xms98::getId).findFirst().orElseThrow();
        assertThatThrownBy(() -> queryService.getAddress("me@fream.com", othersId))
                .isInstanceOf(Xpp56Exception.class);
        assertThatThrownBy(() -> queryService.getAddresses("ghost@fream.com"))
                .isInstanceOf(Xkk16Exception.class);
    }

    @Test
    void searchByRecipientAndPhoneFindsAsEntered() {
        assertThat(queryService.searchAddressesByRecipientName("me@fream.com", "김철수"))
                .extracting(Xms98ResponseDto::getZipCode).containsExactly("13494");
        assertThat(queryService.searchAddressesByPhoneNumber("me@fream.com", "01012345678"))
                .extracting(Xms98ResponseDto::getRecipientName).containsExactly("홍길동");
        // 다른 회원의 배송지는 검색되지 않는다
        assertThat(queryService.searchAddressesByRecipientName("me@fream.com", "이영희")).isEmpty();
    }

    @Test
    void updatedAddressIsReadBackAsEntered() {
        Long kimId = idOf("김철수");
        Xms98UpdateDto update = new Xms98UpdateDto();
        update.setAddressId(kimId);
        update.setAddress("경기도 성남시 수정구 4");
        update.setDetailedAddress("2층");
        commandService.updateAddress("me@fream.com", update);
        newRequest();

        Xms98ResponseDto dto = queryService.getAddress("me@fream.com", kimId);
        assertThat(dto.getRecipientName()).isEqualTo("김철수");
        assertThat(dto.getAddress()).isEqualTo("경기도 성남시 수정구 4");
        assertThat(dto.getDetailedAddress()).isEqualTo("2층");
    }

    @Test
    void instantPurchaseShipsToChosenSavedAddress() {
        Long kimId = idOf("김철수");
        Xjm45 buyer = em.find(Xjm45.class, me.getId());

        Xnd96CommandService orderItems = mock(Xnd96CommandService.class);
        Xfr60CommandService shipments = mock(Xfr60CommandService.class);
        Xhq12CommandService orderService = new Xhq12CommandService(
                orderRepository, mock(Xjt45CommandService.class), shipments, orderItems,
                mock(Xtw92QueryService.class), mock(Xjm45QueryService.class),
                mock(Xms75CommandService.class), queryService, entityManager);

        Xcc44 size = Xcc44.builder().id(11L).build();
        Xzt63 saleBid = Xzt63.builder().id(71L).bidPrice(99000).productSize(size)
                .sale(Xpj42.builder().id(51L).isWarehouseStorage(false).build()).build();
        when(orderItems.createOrderItem(any(Xhq12.class), any(), any(Integer.class)))
                .thenReturn(Xnd96.builder().price(99000).quantity(1).build());
        when(shipments.createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(Xfr60.builder().build());

        Xhq12 order = orderService.createInstantOrder(buyer, saleBid, kimId, false,
                Xgg74RequestDto.builder().impUid("imp_h").paidAmount(99000).userEmail("me@fream.com").build());

        assertThat(order.getId()).isNotNull();
        verify(shipments).createOrderShipment(order, "김철수", "0311234567", "13494", "경기도 성남시 분당구 판교역로 2");
    }
}
