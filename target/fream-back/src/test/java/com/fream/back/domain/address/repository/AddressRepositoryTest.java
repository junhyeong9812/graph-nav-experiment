package com.fream.back.domain.address.repository;

import com.fream.back.domain.address.entity.Address;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AddressRepository 의 소유자 범위 조회, 기본 배송지 조회, 다중 필드(null 무시) 검색 결과를 고정한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class AddressRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AddressRepository addressRepository;

    private User me;
    private User other;
    private Address home;
    private Address office;
    private Address othersHome;

    private User persistUser(String email, String referral) {
        return em.persist(User.builder().email(email).password("pw").referralCode(referral)
                .phoneNumber("01000000000").build());
    }

    private Address persistAddress(User owner, String name, String phone, String zip, String addr, boolean isDefault) {
        return em.persist(Address.builder().user(owner).recipientName(name).phoneNumber(phone)
                .zipCode(zip).address(addr).isDefault(isDefault).build());
    }

    @BeforeEach
    void setUp() {
        me = persistUser("me@fream.com", "REFME");
        other = persistUser("other@fream.com", "REFOT");
        home = persistAddress(me, "N1", "P1", "Z1", "A1", true);
        office = persistAddress(me, "N1", "P2", "Z2", "A2", false);
        othersHome = persistAddress(other, "N1", "P1", "Z1", "A1", true);
        em.flush();
        em.clear();
    }

    @Test
    void findByIdAndUser_isScopedToOwner() {
        assertThat(addressRepository.findByIdAndUser(home.getId(), me)).isPresent();
        assertThat(addressRepository.findByIdAndUser(othersHome.getId(), me)).isEmpty();
    }

    @Test
    void defaultQueries_andCount_arePerUser() {
        assertThat(addressRepository.findByUserAndIsDefaultTrue(me)).get()
                .extracting(Address::getId).isEqualTo(home.getId());
        assertThat(addressRepository.findByUserAndIsDefaultFalse(me))
                .extracting(Address::getId).containsExactly(office.getId());
        assertThat(addressRepository.existsByUserAndIsDefaultTrue(me)).isTrue();
        assertThat(addressRepository.countByUser(me)).isEqualTo(2);
        assertThat(addressRepository.findByUser(other))
                .extracting(Address::getId).containsExactly(othersHome.getId());
    }

    @Test
    void singleFieldSearches_matchExactValue_withinUser() {
        assertThat(addressRepository.findByUserAndRecipientName(me, "N1"))
                .extracting(Address::getId).containsExactlyInAnyOrder(home.getId(), office.getId());
        assertThat(addressRepository.findByUserAndPhoneNumber(me, "P2"))
                .extracting(Address::getId).containsExactly(office.getId());
        assertThat(addressRepository.findByUserAndZipCode(me, "Z")).isEmpty();
        assertThat(addressRepository.findByUserAndAddress(other, "A1"))
                .extracting(Address::getId).containsExactly(othersHome.getId());
    }

    @Test
    void findByUserAndMultipleFields_nullConditionsAreIgnored_andNonNullAreAnded() {
        assertThat(addressRepository.findByUserAndMultipleFields(me, null, null, null, null))
                .extracting(Address::getId).containsExactlyInAnyOrder(home.getId(), office.getId());
        assertThat(addressRepository.findByUserAndMultipleFields(me, "N1", "P1", null, null))
                .extracting(Address::getId).containsExactly(home.getId());
        assertThat(addressRepository.findByUserAndMultipleFields(me, "N1", "P1", "A2", null)).isEmpty();
        assertThat(addressRepository.findByUserAndMultipleFields(me, null, null, null, "Z2"))
                .extracting(Address::getId).containsExactly(office.getId());
    }
}
