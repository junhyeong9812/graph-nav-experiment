package com.fream.back.visible.c01;

import com.fream.back.domain.address.dto.AddressCreateDto;
import com.fream.back.domain.address.entity.Address;
import com.fream.back.domain.address.repository.AddressRepository;
import com.fream.back.domain.address.service.command.AddressCommandService;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderItem;
import com.fream.back.domain.order.service.command.OrderCommandService;
import com.fream.back.domain.order.service.command.OrderItemCommandService;
import com.fream.back.domain.payment.dto.GeneralPaymentRequestDto;
import com.fream.back.domain.payment.dto.PaymentRequestDto;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleBid;
import com.fream.back.domain.shipment.entity.OrderShipment;
import com.fream.back.domain.shipment.service.command.OrderShipmentCommandService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 회원이 배송지를 등록한 뒤, 그 배송지를 골라 즉시 구매하는 흐름.
 * 주문 서비스는 실제 배송지 도메인 서비스와 실제 DB(H2)를 쓰고, 나머지 협력자(결제·배송·주문항목 등)는 목으로 둔다.
 */
@DataJpaTest(properties = {
        "personal-data.encryption.secret-key=0123456789abcdef",
        "personal-data.encryption.iv=fedcba9876543210"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class SavedAddressCheckoutTest {

    private static final String REAL_PACKAGE = "com.fream.back.domain.address";

    @Autowired TestEntityManager em;
    @Autowired AddressRepository addressRepository;
    @Autowired ConfigurableApplicationContext ctx;

    private final Map<Class<?>, Object> mocks = new HashMap<>();

    /** 컨텍스트에 있으면 그 빈을, 없으면 생성자 의존성을 같은 방식으로 채워 실제 객체를 만든다. */
    @SuppressWarnings("unchecked")
    private <T> T real(Class<T> type) {
        ConfigurableListableBeanFactory bf = ctx.getBeanFactory();
        T existing = bf.getBeanProvider(type).getIfAvailable();
        if (existing != null) {
            return existing;
        }
        Constructor<?> c = Arrays.stream(type.getDeclaredConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount)).orElseThrow();
        for (Class<?> p : c.getParameterTypes()) {
            real(p);
        }
        T bean = bf.createBean(type);
        bf.registerSingleton(type.getName(), bean);
        return bean;
    }

    /** 대상 클래스를 만든다. 배송지 도메인 의존성과 컨텍스트 빈은 실제, 나머지는 목. */
    private <T> T assemble(Class<T> type) throws Exception {
        Constructor<?> c = Arrays.stream(type.getDeclaredConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount)).orElseThrow();
        Object[] args = new Object[c.getParameterCount()];
        Class<?>[] types = c.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            Class<?> t = types[i];
            Object bean = ctx.getBeanFactory().getBeanProvider(t).getIfAvailable();
            if (bean != null) {
                args[i] = bean;
            } else if (t.getName().startsWith(REAL_PACKAGE)) {
                args[i] = real(t);
            } else {
                args[i] = mocks.computeIfAbsent(t, Mockito::mock);
            }
        }
        return type.cast(c.newInstance(args));
    }

    @SuppressWarnings("unchecked")
    private <T> T mockOf(Class<T> type) {
        return (T) mocks.get(type);
    }

    private User persistBuyer() {
        return em.persist(User.builder().email("buyer@fream.com").password("pw").referralCode("REFBUY")
                .phoneNumber("01099998888").build());
    }

    private AddressCreateDto addressForm() {
        AddressCreateDto dto = new AddressCreateDto();
        dto.setRecipientName("홍길동");
        dto.setPhoneNumber("01012345678");
        dto.setZipCode("06236");
        dto.setAddress("서울시 강남구 테헤란로 1");
        dto.setDetailedAddress("101동 1001호");
        dto.setIsDefault(true);
        return dto;
    }

    @Test
    void instantPurchaseWithSavedAddress_createsOrderAndShipsToThatAddress() throws Exception {
        User buyer = persistBuyer();
        em.flush();

        // 1) 마이페이지에서 배송지 등록 (별도 요청)
        real(AddressCommandService.class).createAddress("buyer@fream.com", addressForm());
        em.flush();
        em.clear();
        List<Address> saved = addressRepository.findAll();
        assertThat(saved).hasSize(1);
        Long addressId = saved.get(0).getId();

        // 2) 즉시 구매 (별도 요청)
        buyer = em.find(User.class, buyer.getId());
        OrderCommandService orderService = assemble(OrderCommandService.class);

        ProductSize size = ProductSize.builder().id(10L).build();
        Sale sale = Sale.builder().id(50L).isWarehouseStorage(false).build();
        SaleBid saleBid = SaleBid.builder().id(70L).bidPrice(210000).productSize(size).sale(sale).build();
        PaymentRequestDto pay = GeneralPaymentRequestDto.builder()
                .impUid("imp_1").paidAmount(210000).userEmail("buyer@fream.com").build();

        when(mockOf(OrderItemCommandService.class).createOrderItem(any(Order.class), eq(size), eq(210000)))
                .thenReturn(OrderItem.builder().price(210000).quantity(1).build());
        when(mockOf(OrderShipmentCommandService.class).createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(OrderShipment.builder().build());

        Order order = orderService.createInstantOrder(buyer, saleBid, addressId, false, pay);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getTotalAmount()).isEqualTo(210000);
        verify(mockOf(OrderShipmentCommandService.class)).createOrderShipment(
                order, "홍길동", "01012345678", "06236", "서울시 강남구 테헤란로 1");
    }
}
