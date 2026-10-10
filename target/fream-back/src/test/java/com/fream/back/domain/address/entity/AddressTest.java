package com.fream.back.domain.address.entity;

import com.fream.back.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Address 엔티티의 부분 갱신(null 무시)과 User 양방향 연관 편의 메서드를 고정한다.
 */
class AddressTest {

    private Address sample() {
        return Address.builder().recipientName("n").phoneNumber("p").zipCode("z")
                .address("a").detailedAddress("d").isDefault(true).build();
    }

    @Test
    void updateAddress_nullArgumentsKeepCurrentValues() {
        Address address = sample();

        address.updateAddress(null, "p2", null, null, null, null);

        assertThat(address.getRecipientName()).isEqualTo("n");
        assertThat(address.getPhoneNumber()).isEqualTo("p2");
        assertThat(address.getZipCode()).isEqualTo("z");
        assertThat(address.getAddress()).isEqualTo("a");
        assertThat(address.getDetailedAddress()).isEqualTo("d");
        assertThat(address.isDefault()).isTrue();
    }

    @Test
    void updateAddress_allProvided_overwritesEverything_includingDefaultFalse() {
        Address address = sample();

        address.updateAddress("n2", "p2", "z2", "a2", "d2", false);

        assertThat(address.getRecipientName()).isEqualTo("n2");
        assertThat(address.getZipCode()).isEqualTo("z2");
        assertThat(address.getAddress()).isEqualTo("a2");
        assertThat(address.getDetailedAddress()).isEqualTo("d2");
        assertThat(address.isDefault()).isFalse();
    }

    @Test
    void userAddAndRemoveAddress_syncsBothSides() {
        User user = User.builder().email("u@fream.com").build();
        Address address = sample();

        user.addAddress(address);
        assertThat(user.getAddresses()).containsExactly(address);
        assertThat(address.getUser()).isSameAs(user);

        user.removeAddress(address);
        assertThat(user.getAddresses()).isEmpty();
        assertThat(address.getUser()).isNull();
    }
}
