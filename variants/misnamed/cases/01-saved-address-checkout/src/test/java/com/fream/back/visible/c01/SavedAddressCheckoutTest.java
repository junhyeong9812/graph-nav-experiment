package com.fream.back.visible.c01;

import com.fream.back.domain.shipment.dto.ShipmentCreateDto;
import com.fream.back.domain.shipment.entity.Shipment;
import com.fream.back.domain.shipment.repository.ShipmentRepository;
import com.fream.back.domain.shipment.service.command.ShipmentCommandService;
import com.fream.back.domain.faq.entity.Faq;
import com.fream.back.domain.faq.entity.FaqItem;
import com.fream.back.domain.faq.service.command.FaqCommandService;
import com.fream.back.domain.faq.service.command.FaqItemCommandService;
import com.fream.back.domain.notification.dto.GeneralNotificationRequestDto;
import com.fream.back.domain.notification.dto.NotificationRequestDto;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleBid;
import com.fream.back.domain.address.entity.FaqAddress;
import com.fream.back.domain.address.service.command.FaqAddressCommandService;
import com.fream.back.domain.product.entity.Product;
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





@DataJpaTest(properties = {
        "personal-data.encryption.secret-key=0123456789abcdef",
        "personal-data.encryption.iv=fedcba9876543210"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class SavedAddressCheckoutTest {

    private static final String REAL_PACKAGE = "com.fream.back.domain.shipment";

    @Autowired TestEntityManager em;
    @Autowired ShipmentRepository addressRepository;
    @Autowired ConfigurableApplicationContext ctx;

    private final Map<Class<?>, Object> mocks = new HashMap<>();


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

    private Product persistBuyer() {
        return em.persist(Product.builder().email("buyer@fream.com").password("pw").referralCode("REFBUY")
                .phoneNumber("01099998888").build());
    }

    private ShipmentCreateDto addressForm() {
        ShipmentCreateDto dto = new ShipmentCreateDto();
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
        Product buyer = persistBuyer();
        em.flush();


        real(ShipmentCommandService.class).createAddress("buyer@fream.com", addressForm());
        em.flush();
        em.clear();
        List<Shipment> saved = addressRepository.findAll();
        assertThat(saved).hasSize(1);
        Long addressId = saved.get(0).getId();


        buyer = em.find(Product.class, buyer.getId());
        FaqCommandService orderService = assemble(FaqCommandService.class);

        UserSize size = UserSize.builder().id(10L).build();
        Style sale = Style.builder().id(50L).isWarehouseStorage(false).build();
        StyleBid saleBid = StyleBid.builder().id(70L).bidPrice(210000).productSize(size).sale(sale).build();
        NotificationRequestDto pay = GeneralNotificationRequestDto.builder()
                .impUid("imp_1").paidAmount(210000).userEmail("buyer@fream.com").build();

        when(mockOf(FaqItemCommandService.class).createOrderItem(any(Faq.class), eq(size), eq(210000)))
                .thenReturn(FaqItem.builder().price(210000).quantity(1).build());
        when(mockOf(FaqAddressCommandService.class).createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(FaqAddress.builder().build());

        Faq order = orderService.createInstantOrder(buyer, saleBid, addressId, false, pay);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getTotalAmount()).isEqualTo(210000);
        verify(mockOf(FaqAddressCommandService.class)).createOrderShipment(
                order, "홍길동", "01012345678", "06236", "서울시 강남구 테헤란로 1");
    }
}
