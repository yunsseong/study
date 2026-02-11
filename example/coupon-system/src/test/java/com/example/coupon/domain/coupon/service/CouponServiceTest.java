package com.example.coupon.domain.coupon.service;

import com.example.coupon.domain.coupon.dto.CouponCreateRequest;
import com.example.coupon.domain.coupon.dto.CouponResponse;
import com.example.coupon.domain.coupon.entity.Coupon;
import com.example.coupon.domain.coupon.repository.CouponIssueRepository;
import com.example.coupon.domain.coupon.repository.CouponRepository;
import com.example.coupon.global.error.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @AfterEach
    void tearDown() {
        couponIssueRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    @DisplayName("쿠폰을 생성한다")
    void createCoupon() {
        // given
        Coupon coupon = couponRepository.save(
                Coupon.builder()
                        .name("신규 가입 쿠폰")
                        .totalQuantity(100)
                        .build()
        );

        // when
        CouponResponse response = couponService.getCoupon(coupon.getId());

        // then
        assertThat(response.getName()).isEqualTo("신규 가입 쿠폰");
        assertThat(response.getTotalQuantity()).isEqualTo(100);
        assertThat(response.getIssuedQuantity()).isEqualTo(0);
        assertThat(response.getRemainingQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("쿠폰을 정상 발급한다")
    void issue_success() {
        // given
        Coupon coupon = couponRepository.save(
                Coupon.builder()
                        .name("테스트 쿠폰")
                        .totalQuantity(10)
                        .build()
        );

        // when
        couponService.issue(coupon.getId(), 1L);

        // then
        CouponResponse response = couponService.getCoupon(coupon.getId());
        assertThat(response.getIssuedQuantity()).isEqualTo(1);
        assertThat(response.getRemainingQuantity()).isEqualTo(9);
    }

    @Test
    @DisplayName("품절된 쿠폰 발급 시 예외가 발생한다")
    void issue_whenSoldOut_throwsException() {
        // given
        Coupon coupon = couponRepository.save(
                Coupon.builder()
                        .name("1개 한정")
                        .totalQuantity(1)
                        .build()
        );
        couponService.issue(coupon.getId(), 1L);

        // when & then
        assertThatThrownBy(() -> couponService.issue(coupon.getId(), 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("소진");
    }

    @Test
    @DisplayName("중복 발급 시 예외가 발생한다")
    void issue_whenDuplicate_throwsException() {
        // given
        Coupon coupon = couponRepository.save(
                Coupon.builder()
                        .name("중복 테스트")
                        .totalQuantity(10)
                        .build()
        );
        couponService.issue(coupon.getId(), 1L);

        // when & then
        assertThatThrownBy(() -> couponService.issue(coupon.getId(), 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 발급");
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 발급 시 예외가 발생한다")
    void issue_whenNotFound_throwsException() {
        assertThatThrownBy(() -> couponService.issue(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("찾을 수 없습니다");
    }
}
