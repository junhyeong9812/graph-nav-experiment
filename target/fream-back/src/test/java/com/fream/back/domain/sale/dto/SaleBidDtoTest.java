package com.fream.back.domain.sale.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 판매 입찰 응답·카운트 DTO의 계산·표시 값을 고정한다.
 */
class SaleBidDtoTest {

    @Test
    void statusCountTotalAndRatiosRoundToOneDecimal() {
        SaleBidStatusCountDto dto = new SaleBidStatusCountDto(1, 1, 1);

        assertThat(dto.getTotalCount()).isEqualTo(3);
        assertThat(dto.getPendingRatio()).isEqualTo(33.3);
        assertThat(dto.getMatchedRatio()).isEqualTo(33.3);
        assertThat(dto.getCancelledOrCompletedRatio()).isEqualTo(33.3);
    }

    @Test
    void statusCountRatiosAreZeroWhenEmpty() {
        SaleBidStatusCountDto dto = new SaleBidStatusCountDto(0, 0, 0);

        assertThat(dto.getTotalCount()).isZero();
        assertThat(dto.getPendingRatio()).isZero();
        assertThat(dto.getMatchedRatio()).isZero();
        assertThat(dto.getCancelledOrCompletedRatio()).isZero();
    }

    @Test
    void statusCountRatioTwoThirds() {
        SaleBidStatusCountDto dto = new SaleBidStatusCountDto(2, 0, 1);

        assertThat(dto.getPendingRatio()).isEqualTo(66.7);
        assertThat(dto.getCancelledOrCompletedRatio()).isEqualTo(33.3);
    }

    @Test
    void responseFormattedPriceAndKoreanLabels() {
        SaleBidResponseDto dto = SaleBidResponseDto.builder()
                .bidPrice(1234567)
                .saleBidStatus("MATCHED")
                .saleStatus("IN_INSPECTION")
                .build();

        assertThat(dto.getFormattedPrice()).isEqualTo("₩1,234,567");
        assertThat(dto.getSaleBidStatusKorean()).isEqualTo("매칭 완료");
        assertThat(dto.getSaleStatusKorean()).isEqualTo("검수 중");
    }

    @Test
    void responseKoreanLabelsForNullAndUnknown() {
        SaleBidResponseDto nulls = SaleBidResponseDto.builder().build();
        SaleBidResponseDto unknown = SaleBidResponseDto.builder()
                .saleBidStatus("WHATEVER").saleStatus("ODD").build();

        assertThat(nulls.getSaleBidStatusKorean()).isEmpty();
        assertThat(nulls.getSaleStatusKorean()).isEmpty();
        assertThat(unknown.getSaleBidStatusKorean()).isEqualTo("WHATEVER");
        assertThat(unknown.getSaleStatusKorean()).isEqualTo("ODD");
    }
}
