package com.fream.back.domain.style.repository;

import com.fream.back.domain.style.dto.ProfileStyleResponseDto;
import com.fream.back.domain.style.dto.StyleDetailResponseDto;
import com.fream.back.domain.style.dto.StyleFilterRequestDto;
import com.fream.back.domain.style.dto.StyleResponseDto;
import com.fream.back.domain.style.entity.MediaUrl;
import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleComment;
import com.fream.back.domain.style.entity.StyleLike;
import com.fream.back.domain.user.entity.Profile;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * StyleRepositoryCustomImpl 필터·정렬·페이징·상세·프로필별 조회 결과 고정.
 *
 * 데이터:
 *  alice: s1(view 10, 미디어 2개, 좋아요 2, 댓글 1), s2(view 50, 미디어 없음, 좋아요 1)
 *  bob  : s3(view 10, 미디어 1개, 좋아요 0)
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class StyleRepositoryCustomImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private StyleRepository styleRepository;

    private Profile alice;
    private Profile bob;
    private Style s1;
    private Style s2;
    private Style s3;

    @BeforeEach
    void setUp() {
        alice = profile("alice", "alice.png");
        bob = profile("bob", null);

        s1 = style(alice, "first look", 10L);
        s2 = style(alice, "second look", 50L);
        s3 = style(bob, "bob look", 10L);

        media(s1, "s1-a.jpg");
        media(s1, "s1-b.jpg");
        media(s3, "s3-a.jpg");

        like(s1, alice);
        like(s1, bob);
        like(s2, bob);

        em.persist(StyleComment.builder().style(s1).profile(bob).content("nice").build());

        em.flush();
        em.clear();
    }

    private Profile profile(String name, String imageUrl) {
        User user = User.builder()
                .email(name + "@test.com").password("pw").referralCode("REF-" + name).phoneNumber("010" + name)
                .build();
        em.persist(user);
        Profile profile = Profile.builder().user(user).profileName(name).profileImageUrl(imageUrl).build();
        em.persist(profile);
        return profile;
    }

    private Style style(Profile profile, String content, Long viewCount) {
        Style style = Style.builder().profile(profile).content(content).viewCount(viewCount).build();
        em.persist(style);
        return style;
    }

    private void media(Style style, String url) {
        em.persist(MediaUrl.builder().style(style).url(url).build());
    }

    private void like(Style style, Profile profile) {
        em.persist(StyleLike.builder().style(style).profile(profile).build());
    }

    private StyleFilterRequestDto filter(String profileName, String sortBy) {
        StyleFilterRequestDto dto = new StyleFilterRequestDto();
        dto.setProfileName(profileName);
        if (sortBy != null) {
            dto.setSortBy(sortBy);
        }
        return dto;
    }

    @Test
    void filterWithoutConditionsOrdersByLatestAndMapsFirstMediaAndLikeCount() {
        Page<StyleResponseDto> page = styleRepository.filterStyles(filter(null, null), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .extracting(StyleResponseDto::getId, StyleResponseDto::getProfileName, StyleResponseDto::getMediaUrl,
                        StyleResponseDto::getViewCount, StyleResponseDto::getLikeCount)
                .containsExactly(
                        tuple(s3.getId(), "bob", "s3-a.jpg", 10L, 0),
                        tuple(s2.getId(), "alice", null, 50L, 1),
                        tuple(s1.getId(), "alice", "s1-a.jpg", 10L, 2));
        assertThat(page.getContent()).allSatisfy(dto -> {
            assertThat(dto.getLiked()).isFalse();
            assertThat(dto.getInterested()).isFalse();
            assertThat(dto.getHashtags()).isEmpty();
        });
    }

    @Test
    void nullFilterBehavesLikeLatest() {
        Page<StyleResponseDto> page = styleRepository.filterStyles(null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(StyleResponseDto::getId)
                .containsExactly(s3.getId(), s2.getId(), s1.getId());
    }

    @Test
    void filterByProfileName() {
        Page<StyleResponseDto> page = styleRepository.filterStyles(filter("alice", null), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(StyleResponseDto::getId).containsExactly(s2.getId(), s1.getId());
        assertThat(page.getContent().get(0).getProfileImageUrl()).isEqualTo("alice.png");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void filterByUnknownBrandReturnsEmptyPage() {
        StyleFilterRequestDto dto = filter(null, null);
        dto.setBrandName("Nike");

        Page<StyleResponseDto> page = styleRepository.filterStyles(dto, PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void popularSortsByViewCountThenIdDescending() {
        Page<StyleResponseDto> page = styleRepository.filterStyles(filter(null, "popular"), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(StyleResponseDto::getId)
                .containsExactly(s2.getId(), s3.getId(), s1.getId());
    }

    @Test
    void pagingUsesFilteredIdCountAsTotal() {
        Page<StyleResponseDto> page0 = styleRepository.filterStyles(filter(null, null), PageRequest.of(0, 2));
        Page<StyleResponseDto> page1 = styleRepository.filterStyles(filter(null, null), PageRequest.of(1, 2));

        assertThat(page0.getContent()).extracting(StyleResponseDto::getId).containsExactly(s3.getId(), s2.getId());
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page1.getContent()).extracting(StyleResponseDto::getId).containsExactly(s1.getId());
    }

    @Test
    void styleDetailCollectsMediaInIdOrderAndCounts() {
        StyleDetailResponseDto detail = styleRepository.getStyleDetail(s1.getId());

        assertThat(detail.getId()).isEqualTo(s1.getId());
        assertThat(detail.getProfileId()).isEqualTo(alice.getId());
        assertThat(detail.getProfileName()).isEqualTo("alice");
        assertThat(detail.getContent()).isEqualTo("first look");
        assertThat(detail.getMediaUrls()).containsExactly("s1-a.jpg", "s1-b.jpg");
        assertThat(detail.getLikeCount()).isEqualTo(2L);
        assertThat(detail.getCommentCount()).isEqualTo(1L);
        assertThat(detail.getProductInfos()).isEmpty();
        assertThat(detail.getCreatedDate()).isNotNull();
    }

    @Test
    void styleDetailForUnknownIdIsNull() {
        assertThat(styleRepository.getStyleDetail(-1L)).isNull();
    }

    @Test
    void stylesByProfile() {
        Page<ProfileStyleResponseDto> page = styleRepository.getStylesByProfile(alice.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(ProfileStyleResponseDto::getId, ProfileStyleResponseDto::getMediaUrl, ProfileStyleResponseDto::getLikeCount)
                .containsExactlyInAnyOrder(
                        tuple(s1.getId(), "s1-a.jpg", 2L),
                        tuple(s2.getId(), null, 1L));
    }

    @Test
    void styleCountByColorIdsWithoutLinkedOrderItems() {
        assertThat(styleRepository.styleCountByColorIds(List.of())).isEmpty();
        assertThat(styleRepository.styleCountByColorIds(null)).isEmpty();
        assertThat(styleRepository.styleCountByColorIds(List.of(1L, 2L))).isEmpty();
    }
}
