package com.fream.back.visible.c02;

import com.fream.back.domain.m16.dto.Xrt49RequestDto;
import com.fream.back.domain.m16.dto.kafka.Xmr97Event;
import com.fream.back.domain.m16.entity.Xdg64;
import com.fream.back.domain.m16.entity.Xdg64Status;
import com.fream.back.domain.m16.repository.Xdg64Repository;
import com.fream.back.domain.m16.service.kafka.Xdg64EventConsumer;
import com.fream.back.domain.m06.dto.Xqg94RequestDto;
import com.fream.back.domain.m06.entity.Xbf54Info;
import com.fream.back.domain.m06.p05.Xxv33ApiClient;
import com.fream.back.domain.m06.repository.Xbf54Repository;
import com.fream.back.domain.m06.service.query.Xbf54InfoQueryService;
import com.fream.back.domain.m12.service.command.Xhx14CommandService;
import com.fream.back.domain.m13.entity.Xrd81;
import com.fream.back.domain.m13.service.query.Xrd81QueryService;
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

    private static final String REAL_PACKAGE = "com.fream.back.domain.m06.service.command";

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
        Xdg64EventConsumer consumer = assemble(Xdg64EventConsumer.class);

        Xrd81 buyer = Xrd81.builder().id(1L).email("buyer@test.com").build();
        Xdg64 order = Xdg64.builder().id(100L).user(buyer).totalAmount(30000)
                .status(Xdg64Status.PENDING_PAYMENT).build();

        when(mockOf(Xdg64Repository.class).findById(100L)).thenReturn(Optional.of(order));
        when(mockOf(Xrd81QueryService.class).findByEmail("buyer@test.com")).thenReturn(buyer);
        when(mockOf(Xbf54Repository.class).save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockOf(Xbf54InfoQueryService.class).getPaymentInfoEntity(eq("buyer@test.com"), eq(7L)))
                .thenReturn(Xbf54Info.builder().cardNumber("1234567812345678").build());

        when(mockOf(Xxv33ApiClient.class).processCardPayment(any(), anyDouble()))
                .thenReturn(Map.of("status", "failed", "fail_reason", "한도 초과", "imp_uid", "imp_declined"));

        Xrt49RequestDto request = Xrt49RequestDto.builder()
                .paymentRequest(Xqg94RequestDto.builder()
                        .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build())
                .receiverName("홍길동").receiverPhone("01011112222").postalCode("06236").address("서울시 강남구")
                .warehouseStorage(false)
                .build();

        consumer.handleOrderProcessingEvent(
                Xmr97Event.create(100L, "buyer@test.com", request), mock(Acknowledgment.class));


        verify(mockOf(Xxv33ApiClient.class)).processCardPayment(any(), eq(30000.0));

        assertThat(order.getStatus()).isEqualTo(Xdg64Status.PENDING_PAYMENT);
        verify(mockOf(Xhx14CommandService.class), never())
                .createOrderShipment(any(), anyString(), anyString(), anyString(), anyString());
    }
}
