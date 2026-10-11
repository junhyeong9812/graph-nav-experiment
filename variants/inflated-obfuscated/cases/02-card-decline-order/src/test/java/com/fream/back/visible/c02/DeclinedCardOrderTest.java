package com.fream.back.visible.c02;

import com.fream.back.domain.m99.dto.Xsw86RequestDto;
import com.fream.back.domain.m99.dto.kafka.Xrb42Event;
import com.fream.back.domain.m99.entity.Xhq12;
import com.fream.back.domain.m99.entity.Xhq12Status;
import com.fream.back.domain.m99.repository.Xhq12Repository;
import com.fream.back.domain.m99.service.kafka.Xhq12EventConsumer;
import com.fream.back.domain.m64.dto.Xxf47RequestDto;
import com.fream.back.domain.m64.entity.Xjt45Info;
import com.fream.back.domain.m64.p02.Xhs91ApiClient;
import com.fream.back.domain.m64.repository.Xjt45Repository;
import com.fream.back.domain.m64.service.query.Xjt45InfoQueryService;
import com.fream.back.domain.m45.service.command.Xfr60CommandService;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.domain.m22.service.query.Xjm45QueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.support.Acknowledgment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;





class DeclinedCardOrderTest {

    private static final String REAL_PACKAGE = "com.fream.back.domain.m64.service.command";

    private final Map<Class<?>, Object> mocks = new HashMap<>();


    private <T> T assemble(Class<T> type) throws Exception {
        Constructor<?> c = Arrays.stream(type.getDeclaredConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount)).orElseThrow();
        Class<?>[] types = c.getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> t = types[i];
            boolean concrete = !t.isInterface() && !Modifier.isAbstract(t.getModifiers());
            args[i] = (concrete && t.getName().startsWith(REAL_PACKAGE))
                    ? assemble(t)
                    : mocks.computeIfAbsent(t, Mockito::mock);
        }
        return type.cast(c.newInstance(args));
    }

    @SuppressWarnings("unchecked")
    private <T> T mockOf(Class<T> type) {
        return (T) mocks.computeIfAbsent(type, Mockito::mock);
    }

    @Test
    void declinedCard_keepsOrderWaitingForPayment_andCreatesNoShipment() throws Exception {
        Xhq12EventConsumer consumer = assemble(Xhq12EventConsumer.class);

        Xjm45 buyer = Xjm45.builder().id(1L).email("buyer@test.com").build();
        Xhq12 order = Xhq12.builder().id(100L).user(buyer).totalAmount(30000)
                .status(Xhq12Status.PENDING_PAYMENT).build();

        when(mockOf(Xhq12Repository.class).findById(100L)).thenReturn(Optional.of(order));
        when(mockOf(Xjm45QueryService.class).findByEmail("buyer@test.com")).thenReturn(buyer);
        when(mockOf(Xjt45Repository.class).save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockOf(Xjt45InfoQueryService.class).getPaymentInfoEntity(eq("buyer@test.com"), eq(7L)))
                .thenReturn(Xjt45Info.builder().cardNumber("1234567812345678").build());

        when(mockOf(Xhs91ApiClient.class).processCardPayment(any(), anyDouble()))
                .thenReturn(Map.of("status", "failed", "fail_reason", "한도 초과", "imp_uid", "imp_declined"));

        Xsw86RequestDto request = Xsw86RequestDto.builder()
                .paymentRequest(Xxf47RequestDto.builder()
                        .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build())
                .receiverName("홍길동").receiverPhone("01011112222").postalCode("06236").address("서울시 강남구")
                .warehouseStorage(false)
                .build();

        consumer.handleOrderProcessingEvent(
                Xrb42Event.create(100L, "buyer@test.com", request), mock(Acknowledgment.class));


        verify(mockOf(Xhs91ApiClient.class)).processCardPayment(any(), eq(30000.0));

        assertThat(order.getStatus()).isEqualTo(Xhq12Status.PENDING_PAYMENT);
        verify(mockOf(Xfr60CommandService.class), never())
                .createOrderShipment(any(), anyString(), anyString(), anyString(), anyString());
    }
}
