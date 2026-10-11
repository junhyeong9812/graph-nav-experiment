package com.fream.back.hidden.c03;

import com.fream.back.domain.m30.entity.Xft86;
import com.fream.back.domain.m30.entity.Xnr50;
import com.fream.back.domain.m30.entity.Xvm17;
import com.fream.back.domain.m30.entity.Xdt35;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m30.entity.enumType.Xdg35Type;
import com.fream.back.domain.m30.entity.enumType.Xcn23Type;
import com.fream.back.domain.m22.entity.Xjm45;
import jakarta.persistence.EntityManager;

final class Fixtures {
    private final EntityManager em;
    private int seq = 0;

    Fixtures(EntityManager em) {
        this.em = em;
    }

    Xjm45 user(String email) {
        seq++;
        Xjm45 u = Xjm45.builder().email(email).password("pw").referralCode("REF-" + seq)
                .phoneNumber("010-0000-000" + seq).build();
        em.persist(u);
        return u;
    }

    Xft86 brand(String name) {
        Xft86 b = Xft86.builder().name(name).build();
        em.persist(b);
        return b;
    }

    Xnr50 product(String name, Xft86 brand) {
        seq++;
        Xnr50 p = Xnr50.builder().name(name).englishName(name + " EN").releasePrice(100000)
                .modelNumber("MN-" + seq).releaseDate("2024-01-0" + (seq % 9 + 1)).gender(Xdg35Type.UNISEX)
                .brand(brand).build();
        em.persist(p);
        return p;
    }

    Xvm17 color(Xnr50 product, String name, String imageUrl) {
        Xvm17 color = Xvm17.builder().colorName(name).product(product).build();
        em.persist(color);
        if (imageUrl != null) {
            Xdt35 image = Xdt35.builder().imageUrl(imageUrl).productColor(color).build();
            em.persist(image);
            color.addThumbnailImage(image);
        }
        return color;
    }

    Xcc44 size(Xvm17 color, String size, int purchasePrice) {
        Xcc44 ps = Xcc44.builder().sizeType(Xcn23Type.SHOES).size(size)
                .purchasePrice(purchasePrice).salePrice(purchasePrice - 10000).quantity(5).productColor(color).build();
        em.persist(ps);
        return ps;
    }
}
