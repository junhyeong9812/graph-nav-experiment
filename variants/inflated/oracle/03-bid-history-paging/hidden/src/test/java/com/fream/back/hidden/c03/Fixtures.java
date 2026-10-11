package com.fream.back.hidden.c03;

import com.fream.back.domain.product.entity.Brand;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.entity.ProductColor;
import com.fream.back.domain.product.entity.ProductImage;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.product.entity.enumType.GenderType;
import com.fream.back.domain.product.entity.enumType.SizeType;
import com.fream.back.domain.user.entity.User;
import jakarta.persistence.EntityManager;

final class Fixtures {
    private final EntityManager em;
    private int seq = 0;

    Fixtures(EntityManager em) {
        this.em = em;
    }

    User user(String email) {
        seq++;
        User u = User.builder().email(email).password("pw").referralCode("REF-" + seq)
                .phoneNumber("010-0000-000" + seq).build();
        em.persist(u);
        return u;
    }

    Brand brand(String name) {
        Brand b = Brand.builder().name(name).build();
        em.persist(b);
        return b;
    }

    Product product(String name, Brand brand) {
        seq++;
        Product p = Product.builder().name(name).englishName(name + " EN").releasePrice(100000)
                .modelNumber("MN-" + seq).releaseDate("2024-01-0" + (seq % 9 + 1)).gender(GenderType.UNISEX)
                .brand(brand).build();
        em.persist(p);
        return p;
    }

    ProductColor color(Product product, String name, String imageUrl) {
        ProductColor color = ProductColor.builder().colorName(name).product(product).build();
        em.persist(color);
        if (imageUrl != null) {
            ProductImage image = ProductImage.builder().imageUrl(imageUrl).productColor(color).build();
            em.persist(image);
            color.addThumbnailImage(image);
        }
        return color;
    }

    ProductSize size(ProductColor color, String size, int purchasePrice) {
        ProductSize ps = ProductSize.builder().sizeType(SizeType.SHOES).size(size)
                .purchasePrice(purchasePrice).salePrice(purchasePrice - 10000).quantity(5).productColor(color).build();
        em.persist(ps);
        return ps;
    }
}
