package com.fream.back.hidden.c03;

import com.fream.back.domain.m30.dto.Xnr50SearchResponseDto;
import com.fream.back.domain.m30.entity.Xft86;
import com.fream.back.domain.m30.entity.Xnr50;
import com.fream.back.domain.m30.entity.Xvm17;
import com.fream.back.domain.m30.repository.Xdr94Repository;
import com.fream.back.domain.m30.repository.Xgx95Repository;
import com.fream.back.domain.m30.repository.Xgq51;
import com.fream.back.global.config.Xzj77Config;
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
@Import({Xzj77Config.class, Xgx95Repository.class, Xdr94Repository.class})
abstract class ProductSearchFixture {

    @Autowired protected EntityManager em;
    @Autowired protected Xgx95Repository productRepository;
    @Autowired protected Xdr94Repository indexedRepository;

    protected Xft86 nike;
    protected final List<Long> allColors = new ArrayList<>();
    protected final List<Long> colorsWithImage = new ArrayList<>();
    protected final List<Long> nikeColorsWithImage = new ArrayList<>();
    protected final Xgq51 byPrice = new Xgq51("price", "asc");

    @BeforeEach
    void setUp() {
        Fixtures f = new Fixtures(em);
        nike = f.brand("Nike");
        Xft86 adidas = f.brand("Adidas");
        Xnr50 p1 = f.product("Air Max", nike);
        Xnr50 p2 = f.product("Superstar", adidas);
        Xnr50 p3 = f.product("Dunk", nike);

        color(f, p1, "Black", true, 101000, true);
        color(f, p1, "White", true, 102000, true);
        color(f, p2, "Black", true, 103000, false);
        color(f, p2, "Blue", false, 104000, false);
        color(f, p3, "Grey", true, 105000, true);
        color(f, p3, "Red", true, 106000, true);
        em.flush();
        em.clear();
    }

    private void color(Fixtures f, Xnr50 p, String name, boolean withImage, int price, boolean isNike) {
        Xvm17 c = f.color(p, name, withImage ? p.getName() + "-" + name + ".jpg" : null);
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
