package com.fream.back.domain.user.repository;

import com.fream.back.domain.user.dto.SanctionDto;
import com.fream.back.domain.user.entity.*;
import com.fream.back.global.config.QueryDslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository 검색 쿼리(다중 조건·키워드 페이징·등급 집계·활성 수)와
 * UserSanctionRepository 의 만료 대상·이메일 조회·Specification 검색 결과를 고정한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSanctionRepository userSanctionRepository;

    private User alice;
    private User bob;
    private User carol;
    private UserGrade gold;

    private User persistUser(String email, String referral, String phone, Role role, Gender gender,
                             boolean active, boolean verified, UserGrade grade) {
        User u = User.builder()
                .email(email)
                .password("pw")
                .referralCode(referral)
                .phoneNumber(phone)
                .role(role)
                .gender(gender)
                .isActive(active)
                .isVerified(verified)
                .grade(grade)
                .build();
        return em.persist(u);
    }

    private void persistProfile(User user, String profileName) {
        em.persist(Profile.builder().user(user).profileName(profileName).isPublic(true).build());
    }

    @BeforeEach
    void setUp() {
        gold = em.persist(UserGrade.builder().level(3).name("GOLD").pointRate(1.0).build());
        alice = persistUser("alice@fream.com", "REFA", "01011112222", Role.USER, Gender.FEMALE, true, true, gold);
        bob = persistUser("bob@fream.com", "REFB", "01033334444", Role.ADMIN, Gender.MALE, true, false, gold);
        carol = persistUser("carol@other.com", "REFC", "01055556666", Role.USER, Gender.FEMALE, false, true, null);
        persistProfile(alice, "sneakerqueen");
        persistProfile(bob, "bobby");
        em.flush();
        em.clear();
    }

    @Test
    void findByMultipleCriteria_nullIgnored_emailPartialLike_roleExact() {
        assertThat(userRepository.findByMultipleCriteria(null, null, null, null))
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("alice@fream.com", "bob@fream.com", "carol@other.com");

        assertThat(userRepository.findByMultipleCriteria(null, "fream", null, null))
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("alice@fream.com", "bob@fream.com");

        assertThat(userRepository.findByMultipleCriteria(null, "fream", null, Role.USER))
                .extracting(User::getEmail)
                .containsExactly("alice@fream.com");

        assertThat(userRepository.findByMultipleCriteria(carol.getId(), null, "5555", null))
                .extracting(User::getEmail)
                .containsExactly("carol@other.com");
    }

    @Test
    void searchUsers_keywordMatchesProfileName_filtersCombine_andPages() {
        Page<User> byProfileName = userRepository.searchUsers("queen", null, null, null, null, null,
                PageRequest.of(0, 10));
        assertThat(byProfileName.getContent()).extracting(User::getEmail).containsExactly("alice@fream.com");

        Page<User> femaleVerified = userRepository.searchUsers(null, true, null, Gender.FEMALE, null, null,
                PageRequest.of(0, 10, Sort.by("email")));
        assertThat(femaleVerified.getContent()).extracting(User::getEmail)
                .containsExactly("alice@fream.com", "carol@other.com");

        Page<User> inactive = userRepository.searchUsers(null, null, false, null, null, null,
                PageRequest.of(0, 10));
        assertThat(inactive.getContent()).extracting(User::getEmail).containsExactly("carol@other.com");

        Page<User> page = userRepository.searchUsers(null, null, null, null,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                PageRequest.of(1, 2, Sort.by("email")));
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).extracting(User::getEmail).containsExactly("carol@other.com");
    }

    @Test
    void countUsersByGrade_and_countActiveUsers() {
        List<Object[]> rows = userRepository.countUsersByGrade();
        Map<Long, Long> byGrade = rows.stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

        assertThat(byGrade).containsExactly(Map.entry(gold.getId(), 2L));
        assertThat(userRepository.countActiveUsers()).isEqualTo(2);
    }

    private UserSanction persistSanction(User user, SanctionType type, SanctionStatus status,
                                         LocalDateTime start, LocalDateTime end) {
        return em.persist(UserSanction.builder()
                .user(em.find(User.class, user.getId()))
                .reason("spam")
                .type(type)
                .status(status)
                .startDate(start)
                .endDate(end)
                .createdBy("admin@fream.com")
                .build());
    }

    @Test
    void sanction_findExpirableSanctions_activeWithEndDateNotAfterNow() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 10, 12, 0);
        UserSanction ended = persistSanction(alice, SanctionType.TEMPORARY_BAN, SanctionStatus.ACTIVE,
                now.minusDays(5), now.minusDays(1));
        UserSanction endsNow = persistSanction(alice, SanctionType.WARNING, SanctionStatus.ACTIVE,
                now.minusDays(5), now);
        persistSanction(alice, SanctionType.TEMPORARY_BAN, SanctionStatus.ACTIVE, now.minusDays(5), now.plusDays(1));
        persistSanction(bob, SanctionType.PERMANENT_BAN, SanctionStatus.ACTIVE, now.minusDays(5), null);
        persistSanction(bob, SanctionType.TEMPORARY_BAN, SanctionStatus.PENDING, now.minusDays(5), now.minusDays(1));
        em.flush();

        assertThat(userSanctionRepository.findExpirableSanctions(now))
                .extracting(UserSanction::getId)
                .containsExactlyInAnyOrder(ended.getId(), endsNow.getId());
        assertThat(userSanctionRepository.countActiveSanctions()).isEqualTo(4);
        assertThat(userSanctionRepository.countPendingSanctions()).isEqualTo(1);
    }

    @Test
    void sanction_specification_filtersByEmailLikeStatusAndType() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        UserSanction a1 = persistSanction(alice, SanctionType.WARNING, SanctionStatus.PENDING, base, base.plusDays(1));
        persistSanction(alice, SanctionType.TEMPORARY_BAN, SanctionStatus.ACTIVE, base, base.plusDays(3));
        UserSanction b1 = persistSanction(bob, SanctionType.WARNING, SanctionStatus.PENDING, base, base.plusDays(1));
        persistSanction(carol, SanctionType.WARNING, SanctionStatus.PENDING, base, base.plusDays(1));
        em.flush();

        SanctionDto.SanctionSearchRequestDto search = SanctionDto.SanctionSearchRequestDto.builder()
                .email("fream")
                .status(SanctionStatus.PENDING)
                .type(SanctionType.WARNING)
                .build();

        List<UserSanction> result = userSanctionRepository.findAll(UserSanctionSpecifications.withSearchCriteria(search));

        assertThat(result).extracting(UserSanction::getId).containsExactlyInAnyOrder(a1.getId(), b1.getId());

        Page<UserSanction> byEmail = userSanctionRepository.findByUserEmail("alice@fream.com", PageRequest.of(0, 10));
        assertThat(byEmail.getTotalElements()).isEqualTo(2);
    }
}
