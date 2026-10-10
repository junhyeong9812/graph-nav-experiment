package com.fream.back.domain.sale.repository;

import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.entity.ProductColor;
import com.fream.back.domain.product.entity.ProductImage;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.product.entity.enumType.GenderType;
import com.fream.back.domain.product.entity.enumType.SizeType;
import com.fream.back.domain.sale.dto.SaleBidResponseDto;
import com.fream.back.domain.sale.dto.SaleBidStatusCountDto;
import com.fream.back.domain.sale.entity.BidStatus;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleBid;
import com.fream.back.domain.sale.entity.SaleStatus;
import com.fream.back.domain.shipment.entity.SellerShipment;
import com.fream.back.domain.shipment.entity.ShipmentStatus;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SaleBidRepository(QueryDSL 커스텀 포함)와 SaleRepository 조회 결과를 고정한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class SaleBidRepositoryTest {

    @Autowired EntityManager em;
    @Autowired SaleBidRepository saleBidRepository;
    @Autowired SaleRepository saleRepository;

    private User seller;
    private User other;
    private ProductSize size;          // 썸네일 있는 색상의 사이즈
    private ProductSize noThumbSize;   // 썸네일 없는 색상의 사이즈

    private static int seq = 0;

    private User user(String email) {
        seq++;
        User u = User.builder()
                .email(email).password("pw").referralCode("ref-" + email)
                .phoneNumber("010-0000-" + seq).build();
        em.persist(u);
        return u;
    }

    private ProductSize productSize(Product product, String colorName, boolean withThumbnail) {
        ProductColor color = ProductColor.builder().colorName(colorName).product(product).build();
        em.persist(color);
        if (withThumbnail) {
            ProductImage image = ProductImage.builder().imageUrl("thumb-" + colorName + ".jpg").productColor(color).build();
            em.persist(image);
            color.addThumbnailImage(image);
        }
        ProductSize ps = ProductSize.builder()
                .sizeType(SizeType.SHOES).size("260").purchasePrice(100).salePrice(90).quantity(1)
                .productColor(color).build();
        em.persist(ps);
        return ps;
    }

    private Sale sale(User s, ProductSize ps, SaleStatus status) {
        Sale sale = Sale.builder().seller(s).productSize(ps).status(status).build();
        em.persist(sale);
        return sale;
    }

    private SaleBid bid(User s, ProductSize ps, int price, BidStatus status, Sale sale, LocalDateTime created) {
        SaleBid bid = SaleBid.builder().seller(s).productSize(ps).bidPrice(price).status(status).sale(sale).build();
        em.persist(bid);
        em.flush();
        em.createNativeQuery("update sale_bid set created_date = ? where id = ?")
                .setParameter(1, created).setParameter(2, bid.getId()).executeUpdate();
        return bid;
    }

    private SaleBid b1, b2, b3, b4, bNoThumb, bOther;
    private Sale sale1;

    @BeforeEach
    void setUp() {
        seller = user("seller@test.com");
        other = user("other@test.com");
        Product product = Product.builder().name("조던").englishName("Jordan").releasePrice(200000)
                .modelNumber("M-1").releaseDate("2024-01-01").gender(GenderType.UNISEX).build();
        em.persist(product);
        size = productSize(product, "Black", true);
        noThumbSize = productSize(product, "White", false);

        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 0, 0);
        sale1 = sale(seller, size, SaleStatus.PENDING_SHIPMENT);
        em.persist(SellerShipment.builder().sale(sale1).courier("CJ").trackingNumber("T1").status(ShipmentStatus.IN_TRANSIT).build());
        Sale sale2 = sale(seller, size, SaleStatus.SOLD);

        b1 = bid(seller, size, 1000, BidStatus.PENDING, null, base.plusDays(1));
        b2 = bid(seller, size, 2000, BidStatus.MATCHED, sale1, base.plusDays(2));
        b3 = bid(seller, size, 3000, BidStatus.COMPLETED, sale2, base.plusDays(3));
        b4 = bid(seller, size, 4000, BidStatus.CANCELLED, null, base.plusDays(4));
        bNoThumb = bid(seller, noThumbSize, 5000, BidStatus.PENDING, null, base.plusDays(5));
        bOther = bid(other, size, 6000, BidStatus.PENDING, null, base.plusDays(6));
        em.flush();
        em.clear();
    }

    private List<Long> ids(Page<SaleBidResponseDto> page) {
        return page.getContent().stream().map(SaleBidResponseDto::getSaleBidId).toList();
    }

    @Test
    void filtersBySellerEmailAndOrdersByCreatedDateDesc() {
        Page<SaleBidResponseDto> page = saleBidRepository.findSaleBidsByFilters(
                "seller@test.com", null, null, PageRequest.of(0, 10));

        // 썸네일 없는 색상의 입찰은 본문에서 빠진다. 마지막 페이지라 PageImpl이 total을 본문 수(4)로 보정한다
        assertThat(ids(page)).containsExactly(b4.getId(), b3.getId(), b2.getId(), b1.getId());
        assertThat(page.getTotalElements()).isEqualTo(4);
    }

    @Test
    void projectsProductSaleAndShipmentFields() {
        Page<SaleBidResponseDto> page = saleBidRepository.findSaleBidsByFilters(
                "seller@test.com", "MATCHED", null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        SaleBidResponseDto dto = page.getContent().get(0);
        assertThat(dto.getSaleBidId()).isEqualTo(b2.getId());
        assertThat(dto.getProductName()).isEqualTo("조던");
        assertThat(dto.getProductEnglishName()).isEqualTo("Jordan");
        assertThat(dto.getSize()).isEqualTo("260");
        assertThat(dto.getColorName()).isEqualTo("Black");
        assertThat(dto.getThumbnailImageUrl()).isEqualTo("thumb-Black.jpg");
        assertThat(dto.getBidPrice()).isEqualTo(2000);
        assertThat(dto.getSaleBidStatus()).isEqualTo("MATCHED");
        assertThat(dto.getSaleStatus()).isEqualTo("PENDING_SHIPMENT");
        assertThat(dto.getShipmentStatus()).isEqualTo("IN_TRANSIT");
        assertThat(dto.isInstantSale()).isFalse();
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void bidWithoutSaleHasNullSaleAndShipmentStatus() {
        Page<SaleBidResponseDto> page = saleBidRepository.findSaleBidsByFilters(
                "seller@test.com", "CANCELLED", null, PageRequest.of(0, 10));

        assertThat(ids(page)).containsExactly(b4.getId());
        assertThat(page.getContent().get(0).getSaleStatus()).isNull();
        assertThat(page.getContent().get(0).getShipmentStatus()).isNull();
    }

    @Test
    void filtersBySaleStatus() {
        Page<SaleBidResponseDto> page = saleBidRepository.findSaleBidsByFilters(
                "seller@test.com", null, "SOLD", PageRequest.of(0, 10));

        assertThat(ids(page)).containsExactly(b3.getId());
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void pagingAppliesOffsetAndLimit() {
        Page<SaleBidResponseDto> page = saleBidRepository.findSaleBidsByFilters(
                "seller@test.com", null, null, PageRequest.of(1, 2));

        // count 쿼리는 상품·썸네일 조인이 없어 썸네일 없는 입찰까지 5건으로 센다
        assertThat(ids(page)).containsExactly(b2.getId(), b1.getId());
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);

        Page<SaleBidResponseDto> last = saleBidRepository.findSaleBidsByFilters(
                "seller@test.com", null, null, PageRequest.of(2, 2));
        assertThat(last.getContent()).isEmpty();
        assertThat(last.getTotalElements()).isEqualTo(5);
    }

    @Test
    void unknownEmailReturnsEmptyPage() {
        Page<SaleBidResponseDto> page = saleBidRepository.findSaleBidsByFilters(
                "nobody@test.com", null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void countsByStatusGroups() {
        SaleBidStatusCountDto counts = saleBidRepository.countSaleBidsByStatus("seller@test.com");

        assertThat(counts.getPendingCount()).isEqualTo(2);
        assertThat(counts.getMatchedCount()).isEqualTo(1);
        assertThat(counts.getCancelledOrCompletedCount()).isEqualTo(2);
    }

    @Test
    void findSaleBidByIdChecksOwnerEmail() {
        assertThat(saleBidRepository.findSaleBidById(b2.getId(), "seller@test.com"))
                .isNotNull()
                .extracting(SaleBidResponseDto::getBidPrice).isEqualTo(2000);
        assertThat(saleBidRepository.findSaleBidById(b2.getId(), "other@test.com")).isNull();
        assertThat(saleBidRepository.findSaleBidById(bNoThumb.getId(), "seller@test.com")).isNull();
    }

    @Test
    void derivedFinders() {
        assertThat(saleBidRepository.findBySale_Id(sale1.getId())).get()
                .extracting(SaleBid::getId).isEqualTo(b2.getId());
        assertThat(saleBidRepository.findByOrder_Id(12345L)).isEmpty();
        assertThat(saleRepository.findBySeller_EmailAndStatus("seller@test.com", SaleStatus.SOLD))
                .hasSize(1);
        assertThat(saleRepository.findBySeller_EmailAndStatus("other@test.com", SaleStatus.SOLD))
                .isEmpty();
    }
}
