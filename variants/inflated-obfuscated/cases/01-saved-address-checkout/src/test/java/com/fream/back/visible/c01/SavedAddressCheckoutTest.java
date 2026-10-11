package com.fream.back.visible.c01;

import com.fream.back.domain.m53.dto.Xms98CreateDto;
import com.fream.back.domain.m53.entity.Xms98;
import com.fream.back.domain.m53.repository.Xms98Repository;
import com.fream.back.domain.m53.service.command.Xms98CommandService;
import com.fream.back.domain.m99.entity.Xhq12;
import com.fream.back.domain.m99.entity.Xnd96;
import com.fream.back.domain.m99.service.command.Xhq12CommandService;
import com.fream.back.domain.m99.service.command.Xnd96CommandService;
import com.fream.back.domain.m64.dto.Xgg74RequestDto;
import com.fream.back.domain.m64.dto.Xjt45RequestDto;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m85.entity.Xpj42;
import com.fream.back.domain.m85.entity.Xzt63;
import com.fream.back.domain.m45.entity.Xfr60;
import com.fream.back.domain.m45.service.command.Xfr60CommandService;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.global.config.Xzj77Config;
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
@Import(Xzj77Config.class)
class SavedAddressCheckoutTest {

    private static final String REAL_PACKAGE = "com.fream.back.domain.m53";

    @Autowired TestEntityManager em;
    @Autowired Xms98Repository addressRepository;
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

    private Xjm45 persistBuyer() {
        return em.persist(Xjm45.builder().email("buyer@fream.com").password("pw").referralCode("REFBUY")
                .phoneNumber("01099998888").build());
    }

    private Xms98CreateDto addressForm() {
        Xms98CreateDto dto = new Xms98CreateDto();
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
        Xjm45 buyer = persistBuyer();
        em.flush();


        real(Xms98CommandService.class).createAddress("buyer@fream.com", addressForm());
        em.flush();
        em.clear();
        List<Xms98> saved = addressRepository.findAll();
        assertThat(saved).hasSize(1);
        Long addressId = saved.get(0).getId();


        buyer = em.find(Xjm45.class, buyer.getId());
        Xhq12CommandService orderService = assemble(Xhq12CommandService.class);

        Xcc44 size = Xcc44.builder().id(10L).build();
        Xpj42 sale = Xpj42.builder().id(50L).isWarehouseStorage(false).build();
        Xzt63 saleBid = Xzt63.builder().id(70L).bidPrice(210000).productSize(size).sale(sale).build();
        Xjt45RequestDto pay = Xgg74RequestDto.builder()
                .impUid("imp_1").paidAmount(210000).userEmail("buyer@fream.com").build();

        when(mockOf(Xnd96CommandService.class).createOrderItem(any(Xhq12.class), eq(size), eq(210000)))
                .thenReturn(Xnd96.builder().price(210000).quantity(1).build());
        when(mockOf(Xfr60CommandService.class).createOrderShipment(any(), any(), any(), any(), any()))
                .thenReturn(Xfr60.builder().build());

        Xhq12 order = orderService.createInstantOrder(buyer, saleBid, addressId, false, pay);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getTotalAmount()).isEqualTo(210000);
        verify(mockOf(Xfr60CommandService.class)).createOrderShipment(
                order, "홍길동", "01012345678", "06236", "서울시 강남구 테헤란로 1");
    }
}
