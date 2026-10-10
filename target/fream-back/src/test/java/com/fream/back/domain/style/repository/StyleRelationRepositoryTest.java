package com.fream.back.domain.style.repository;

import com.fream.back.domain.style.entity.Hashtag;
import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleHashtag;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 좋아요·해시태그 리포지토리의 JPQL·파생 쿼리 결과 고정.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class StyleRelationRepositoryTest {

    @Autowired private EntityManager em;
    @Autowired private StyleLikeRepository styleLikeRepository;
    @Autowired private HashtagRepository hashtagRepository;
    @Autowired private StyleHashtagRepository styleHashtagRepository;

    private Profile alice;
    private Profile bob;
    private Style s1;
    private Style s2;
    private Hashtag ootd;

    @BeforeEach
    void setUp() {
        alice = profile("alice");
        bob = profile("bob");
        s1 = style(alice);
        s2 = style(alice);
        em.persist(StyleLike.builder().style(s1).profile(bob).build());
        em.persist(StyleLike.builder().style(s2).profile(bob).build());
        em.persist(StyleLike.builder().style(s1).profile(alice).build());

        ootd = hashtag("ootd", 5L);
        hashtag("OOTD_daily", 9L);
        hashtag("dailylook", 1L);
        hashtag("street", 20L);
        em.persist(StyleHashtag.builder().style(s1).hashtag(ootd).build());
        em.persist(StyleHashtag.builder().style(s2).hashtag(ootd).build());

        em.flush();
        em.clear();
    }

    private Profile profile(String name) {
        User user = User.builder().email(name + "@t.com").password("pw").referralCode("R-" + name).phoneNumber("010").build();
        em.persist(user);
        Profile profile = Profile.builder().user(user).profileName(name).build();
        em.persist(profile);
        return profile;
    }

    private Style style(Profile profile) {
        Style style = Style.builder().profile(profile).content("c").viewCount(0L).build();
        em.persist(style);
        return style;
    }

    private Hashtag hashtag(String name, Long count) {
        Hashtag hashtag = Hashtag.builder().name(name).count(count).build();
        em.persist(hashtag);
        return hashtag;
    }

    @Test
    void likeExistenceCountAndLikedIdsByProfile() {
        assertThat(styleLikeRepository.existsByStyleIdAndProfileId(s1.getId(), bob.getId())).isTrue();
        assertThat(styleLikeRepository.existsByStyleIdAndProfileId(s2.getId(), alice.getId())).isFalse();
        assertThat(styleLikeRepository.countByStyleId(s1.getId())).isEqualTo(2L);
        assertThat(styleLikeRepository.findStyleIdsByProfileIdAndStyleIdIn(alice.getId(), List.of(s1.getId(), s2.getId())))
                .containsExactly(s1.getId());
        assertThat(styleLikeRepository.findStyleIdsByProfileIdAndStyleIdIn(bob.getId(), List.of(s1.getId(), s2.getId())))
                .containsExactlyInAnyOrder(s1.getId(), s2.getId());
    }

    @Test
    void keywordSearchIsCaseInsensitiveContainsOrderedByCountDesc() {
        assertThat(hashtagRepository.findByNameContainingKeywordOrderByCountDesc("ootd", PageRequest.of(0, 10)))
                .extracting(Hashtag::getName)
                .containsExactly("OOTD_daily", "ootd");
        assertThat(hashtagRepository.findByNameContainingKeywordOrderByCountDesc("daily", PageRequest.of(0, 10)))
                .extracting(Hashtag::getName)
                .containsExactly("OOTD_daily", "dailylook");
    }

    @Test
    void prefixSearchIsCaseSensitiveAndPopularOrdersByCount() {
        assertThat(hashtagRepository.findByNameStartingWithOrderByCountDesc("oo", PageRequest.of(0, 10)))
                .extracting(Hashtag::getName)
                .containsExactly("ootd");
        assertThat(hashtagRepository.findAllByOrderByCountDesc(PageRequest.of(0, 2)).getContent())
                .extracting(Hashtag::getName)
                .containsExactly("street", "OOTD_daily");
    }

    @Test
    void styleHashtagCountAndLookup() {
        assertThat(styleHashtagRepository.countByHashtagId(ootd.getId())).isEqualTo(2L);
        assertThat(styleHashtagRepository.findByStyleIdAndHashtagId(s1.getId(), ootd.getId())).isPresent();
        assertThat(styleHashtagRepository.findStyleIdsByHashtagId(ootd.getId(), PageRequest.of(0, 10)).getContent())
                .containsExactlyInAnyOrder(s1.getId(), s2.getId());
    }
}
