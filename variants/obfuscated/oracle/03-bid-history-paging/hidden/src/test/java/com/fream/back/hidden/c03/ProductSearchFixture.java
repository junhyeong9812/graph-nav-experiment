package com.fream.back.hidden.c03;

import com.fream.back.domain.m02.dto.Xkr46SearchResponseDto;
import com.fream.back.domain.m02.entity.Xfw75;
import com.fream.back.domain.m02.entity.Xkr46;
import com.fream.back.domain.m02.entity.Xfp92;
import com.fream.back.domain.m02.repository.Xvw73Repository;
import com.fream.back.domain.m02.repository.Xmb97Repository;
import com.fream.back.domain.m02.repository.Xpk87;
import com.fream.back.global.config.Xsd33Config;
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
@Import({Xsd33Config.class, Xmb97Repository.class, Xvw73Repository.class})
abstract class ProductSearchFixture {

    @Autowired protected EntityManager em;
    @Autowired protected Xmb97Repository productRepository;
    @Autowired protected Xvw73Repository indexedRepository;

    protected Xfw75 nike;
    protected final List<Long> allColors = new ArrayList<>();
    protected final List<Long> colorsWithImage = new ArrayList<>();
    protected final List<Long> nikeColorsWithImage = new ArrayList<>();
    protected final Xpk87 byPrice = new Xpk87("price", "asc");

    @BeforeEach
    void setUp() {
        Fixtures f = new Fixtures(em);
        nike = f.brand("Nike");
        Xfw75 adidas = f.brand("Adidas");
        Xkr46 p1 = f.product("Air Max", nike);
        Xkr46 p2 = f.product("Superstar", adidas);
        Xkr46 p3 = f.product("Dunk", nike);

        color(f, p1, "Black", true, 101000, true);
        color(f, p1, "White", true, 102000, true);
        color(f, p2, "Black", true, 103000, false);
        color(f, p2, "Blue", false, 104000, false);
        color(f, p3, "Grey", true, 105000, true);
        color(f, p3, "Red", true, 106000, true);
        em.flush();
        em.clear();
    }

    private void color(Fixtures f, Xkr46 p, String name, boolean withImage, int price, boolean isNike) {
        Xfp92 c = f.color(p, name, withImage ? p.getName() + "-" + name + ".jpg" : null);
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
