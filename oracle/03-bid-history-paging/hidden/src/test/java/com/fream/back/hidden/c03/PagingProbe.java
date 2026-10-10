package com.fream.back.hidden.c03;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 페이지를 처음부터 끝까지 넘기며 각 페이지가 보고한 total과 실제로 받은 항목을 모은다.
 */
final class PagingProbe<T, K> {

    final List<Long> totals = new ArrayList<>();
    final List<K> keys = new ArrayList<>();

    static <T, K> PagingProbe<T, K> walk(IntFunction<Page<T>> fetch, Function<T, K> key) {
        PagingProbe<T, K> probe = new PagingProbe<>();
        for (int page = 0; page < 50; page++) {
            Page<T> result = fetch.apply(page);
            probe.totals.add(result.getTotalElements());
            if (result.getContent().isEmpty()) {
                return probe;
            }
            result.getContent().forEach(item -> probe.keys.add(key.apply(item)));
        }
        throw new AssertionError("50페이지 안에 끝나지 않음");
    }

    /** 모든 페이지의 total이 넘겨 본 항목 수와 같고, 항목이 중복되지 않는다. */
    void assertConsistent() {
        assertThat(keys).doesNotHaveDuplicates();
        assertThat(totals)
                .as("페이지별 total %s, 넘겨 본 항목 수 %d", totals, keys.size())
                .allSatisfy(total -> assertThat(total).isEqualTo((long) keys.size()));
    }
}
