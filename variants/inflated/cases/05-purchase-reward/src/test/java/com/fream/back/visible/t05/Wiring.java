package com.fream.back.visible.t05;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 테스트용 객체 조립기.
 *
 * - 리포지토리·EntityManager 등 테스트 컨텍스트(@DataJpaTest)에 있는 빈은 그대로 쓴다.
 * - com.fream.back.domain 아래의 @Service/@Component 클래스는 실제 객체로 생성자 주입해 만든다.
 * - 그 밖의 의존(외부 연동·Redis·WebSocket 등)은 Mockito mock으로 채운다.
 * - ApplicationEventPublisher로 발행한 이벤트는 domain 아래 @EventListener/@TransactionalEventListener 메서드로 즉시 전달한다.
 */
final class Wiring {

    private static final String DOMAIN = "com.fream.back.domain";

    private final ApplicationContext ctx;
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Set<Class<?>> building = new HashSet<>();
    private List<Class<?>> listenerClasses;

    Wiring(ApplicationContext ctx) {
        this.ctx = ctx;
        instances.put(ApplicationEventPublisher.class, (ApplicationEventPublisher) this::dispatch);
    }

    <T> Wiring with(Class<T> type, T instance) {
        instances.put(type, instance);
        return this;
    }

    @SuppressWarnings("unchecked")
    <T> T get(Class<T> type) {
        Object existing = instances.get(type);
        if (existing != null) {
            return (T) existing;
        }
        if (type == String.class) {
            return (T) "";
        }
        if (type.isPrimitive()) {
            return (T) defaultPrimitive(type);
        }
        Object bean = ctx.getBeanProvider(type).getIfUnique();
        if (bean != null) {
            return (T) bean;
        }
        Object created;
        if (isDomainComponent(type) && !building.contains(type)) {
            building.add(type);
            try {
                created = construct(type);
            } finally {
                building.remove(type);
            }
        } else {
            created = Mockito.mock(type);
        }
        instances.put(type, created);
        return (T) created;
    }

    private static boolean isDomainComponent(Class<?> t) {
        return t.getName().startsWith(DOMAIN + ".")
                && !t.isInterface()
                && !Modifier.isAbstract(t.getModifiers())
                && org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation(t, Component.class);
    }

    private Object construct(Class<?> type) {
        try {
            Constructor<?> ctor = null;
            for (Constructor<?> c : type.getDeclaredConstructors()) {
                if (c.isAnnotationPresent(Autowired.class)) {
                    ctor = c;
                }
            }
            if (ctor == null) {
                ctor = java.util.Arrays.stream(type.getDeclaredConstructors())
                        .max(Comparator.comparingInt(Constructor::getParameterCount))
                        .orElseThrow();
            }
            Class<?>[] params = ctor.getParameterTypes();
            Object[] args = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                args[i] = get(params[i]);
            }
            ctor.setAccessible(true);
            Object obj = ctor.newInstance(args);
            instances.put(type, obj);
            for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.isAnnotationPresent(Autowired.class) && !Modifier.isStatic(f.getModifiers())) {
                        f.setAccessible(true);
                        if (f.get(obj) == null) {
                            f.set(obj, get(f.getType()));
                        }
                    }
                }
            }
            return obj;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot build " + type.getName(), e);
        }
    }

    private void dispatch(Object event) {
        Object payload = (event instanceof PayloadApplicationEvent<?> p) ? p.getPayload() : event;
        for (Class<?> listenerType : listenerClasses()) {
            for (Method m : listenerType.getDeclaredMethods()) {
                boolean listener = m.isAnnotationPresent(EventListener.class)
                        || m.isAnnotationPresent(TransactionalEventListener.class);
                if (!listener || m.getParameterCount() != 1) {
                    continue;
                }
                Class<?> p = m.getParameterTypes()[0];
                Object arg = p.isInstance(payload) ? payload
                        : (event instanceof ApplicationEvent && p.isInstance(event)) ? event : null;
                if (arg == null) {
                    continue;
                }
                try {
                    m.setAccessible(true);
                    m.invoke(get(listenerType), arg);
                } catch (InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if (t instanceof RuntimeException re) {
                        throw re;
                    }
                    throw new IllegalStateException(t);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private List<Class<?>> listenerClasses() {
        if (listenerClasses == null) {
            listenerClasses = new ArrayList<>();
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));
            scanner.findCandidateComponents(DOMAIN).forEach(bd -> {
                try {
                    Class<?> c = Class.forName(bd.getBeanClassName());
                    for (Method m : c.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(EventListener.class)
                                || m.isAnnotationPresent(TransactionalEventListener.class)) {
                            listenerClasses.add(c);
                            break;
                        }
                    }
                } catch (ClassNotFoundException | LinkageError ignored) {
                    // 로드할 수 없는 후보는 건너뛴다
                }
            });
        }
        return listenerClasses;
    }

    private static Object defaultPrimitive(Class<?> t) {
        if (t == boolean.class) return false;
        if (t == char.class) return '\0';
        if (t == byte.class) return (byte) 0;
        if (t == short.class) return (short) 0;
        if (t == int.class) return 0;
        if (t == long.class) return 0L;
        if (t == float.class) return 0f;
        return 0d;
    }
}
