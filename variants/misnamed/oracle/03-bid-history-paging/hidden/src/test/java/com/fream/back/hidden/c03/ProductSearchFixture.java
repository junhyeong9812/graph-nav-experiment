package com.fream.back.hidden.c03;

import com.fream.back.domain.user.dto.UserSearchResponseDto;
import com.fream.back.domain.user.entity.Brand;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.entity.UserColor;
import com.fream.back.domain.user.repository.IndexedUserRepository;
import com.fream.back.domain.user.repository.UserQueryDslRepository;
import com.fream.back.domain.user.repository.SortOption;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 상품 검색 지점(3, 4) 공용 데이터: 상품 3개 x 색상 2개, 한 색상은 썸네일이 없다. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({QueryDslConfig.class, UserQueryDslRepository.class, IndexedUserRepository.class})
abstract class ProductSearchFixture {

    @Autowired protected EntityManager em;
    @Autowired protected UserQueryDslRepository productRepository;
    @Autowired protected IndexedUserRepository indexedRepository;

    protected Brand nike;
    protected final List<Long> allColors = new ArrayList<>();
    protected final List<Long> colorsWithImage = new ArrayList<>();
    protected final List<Long> nikeColorsWithImage = new ArrayList<>();
    protected final SortOption byPrice = new SortOption("price", "asc");

    @BeforeEach
    void setUp() {
        Fixtures f = new Fixtures(em);
        nike = f.brand("Nike");
        Brand adidas = f.brand("Adidas");
        User p1 = f.product("Air Max", nike);
        User p2 = f.product("Superstar", adidas);
        User p3 = f.product("Dunk", nike);

        color(f, p1, "Black", true, 101000, true);
        color(f, p1, "White", true, 102000, true);
        color(f, p2, "Black", true, 103000, false);
        color(f, p2, "Blue", false, 104000, false);
        color(f, p3, "Grey", true, 105000, true);
        color(f, p3, "Red", true, 106000, true);
        em.flush();
        em.clear();
    }

    private void color(Fixtures f, User p, String name, boolean withImage, int price, boolean isNike) {
        UserColor c = f.color(p, name, withImage ? p.getName() + "-" + name + ".jpg" : null);
        f.size(c, "250", price);
        f.size(c, "260", price + 500);
        allColors.add(c.getId());
        if (withImage) {
            colorsWithImage.add(c.getId());
            if (isNike) {
                nikeColorsWithImage.add(c.getId());
            }
        }
    }
}
