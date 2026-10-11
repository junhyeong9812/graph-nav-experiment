package com.fream.back.hidden.c03;

import com.fream.back.domain.m02.entity.Xfw75;
import com.fream.back.domain.m02.entity.Xkr46;
import com.fream.back.domain.m02.entity.Xfp92;
import com.fream.back.domain.m02.entity.Xzq99;
import com.fream.back.domain.m02.entity.Xdh20;
import com.fream.back.domain.m02.entity.enumType.Xbj22Type;
import com.fream.back.domain.m02.entity.enumType.Xnp24Type;
import com.fream.back.domain.m13.entity.Xrd81;
import jakarta.persistence.EntityManager;

final class Fixtures {
    private final EntityManager em;
    private int seq = 0;

    Fixtures(EntityManager em) {
        this.em = em;
    }

    Xrd81 user(String email) {
        seq++;
        Xrd81 u = Xrd81.builder().email(email).password("pw").referralCode("REF-" + seq)
                .phoneNumber("010-0000-000" + seq).build();
        em.persist(u);
        return u;
    }

    Xfw75 brand(String name) {
        Xfw75 b = Xfw75.builder().name(name).build();
        em.persist(b);
        return b;
    }

    Xkr46 product(String name, Xfw75 brand) {
        seq++;
        Xkr46 p = Xkr46.builder().name(name).englishName(name + " EN").releasePrice(100000)
                .modelNumber("MN-" + seq).releaseDate("2024-01-0" + (seq % 9 + 1)).gender(Xbj22Type.UNISEX)
                .brand(brand).build();
        em.persist(p);
        return p;
    }

    Xfp92 color(Xkr46 product, String name, String imageUrl) {
        Xfp92 color = Xfp92.builder().colorName(name).product(product).build();
        em.persist(color);
        if (imageUrl != null) {
            Xzq99 image = Xzq99.builder().imageUrl(imageUrl).productColor(color).build();
            em.persist(image);
            color.addThumbnailImage(image);
        }
        return color;
    }

    Xdh20 size(Xfp92 color, String size, int purchasePrice) {
        Xdh20 ps = Xdh20.builder().sizeType(Xnp24Type.SHOES).size(size)
                .purchasePrice(purchasePrice).salePrice(purchasePrice - 10000).quantity(5).productColor(color).build();
        em.persist(ps);
        return ps;
    }
}
