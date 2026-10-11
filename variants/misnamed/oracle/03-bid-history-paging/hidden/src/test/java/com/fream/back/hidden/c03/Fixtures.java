package com.fream.back.hidden.c03;

import com.fream.back.domain.user.entity.Brand;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.entity.UserColor;
import com.fream.back.domain.user.entity.UserImage;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.user.entity.enumType.GenderType;
import com.fream.back.domain.user.entity.enumType.SizeType;
import com.fream.back.domain.product.entity.Product;
import jakarta.persistence.EntityManager;

final class Fixtures {
    private final EntityManager em;
    private int seq = 0;

    Fixtures(EntityManager em) {
        this.em = em;
    }

    Product user(String email) {
        seq++;
        Product u = Product.builder().email(email).password("pw").referralCode("REF-" + seq)
                .phoneNumber("010-0000-000" + seq).build();
        em.persist(u);
        return u;
    }

    Brand brand(String name) {
        Brand b = Brand.builder().name(name).build();
        em.persist(b);
        return b;
    }

    User product(String name, Brand brand) {
        seq++;
        User p = User.builder().name(name).englishName(name + " EN").releasePrice(100000)
                .modelNumber("MN-" + seq).releaseDate("2024-01-0" + (seq % 9 + 1)).gender(GenderType.UNISEX)
                .brand(brand).build();
        em.persist(p);
        return p;
    }

    UserColor color(User product, String name, String imageUrl) {
        UserColor color = UserColor.builder().colorName(name).product(product).build();
        em.persist(color);
        if (imageUrl != null) {
            UserImage image = UserImage.builder().imageUrl(imageUrl).productColor(color).build();
            em.persist(image);
            color.addThumbnailImage(image);
        }
        return color;
    }

    UserSize size(UserColor color, String size, int purchasePrice) {
        UserSize ps = UserSize.builder().sizeType(SizeType.SHOES).size(size)
                .purchasePrice(purchasePrice).salePrice(purchasePrice - 10000).quantity(5).productColor(color).build();
        em.persist(ps);
        return ps;
    }
}
