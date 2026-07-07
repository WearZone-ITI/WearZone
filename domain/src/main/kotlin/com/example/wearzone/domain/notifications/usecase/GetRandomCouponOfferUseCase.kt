package com.example.wearzone.domain.notifications.usecase

import com.example.wearzone.domain.notifications.model.CouponOffer

/**
 * Returns a random coupon offer to be surfaced to the customer via a
 * periodic reminder notification. The pool is static for now; when a
 * "marketing offers" backend endpoint exists this can be swapped for a
 * repository-backed implementation without touching any caller.
 */
class GetRandomCouponOfferUseCase {

    operator fun invoke(): CouponOffer = COUPON_POOL.random()

    private companion object {
        val COUPON_POOL = listOf(
            CouponOffer(code = "WEAR10", discountPercentage = 10),
            CouponOffer(code = "WEAR15", discountPercentage = 15),
            CouponOffer(code = "STYLE20", discountPercentage = 20),
            CouponOffer(code = "ZONE25", discountPercentage = 25),
            CouponOffer(code = "FASHION30", discountPercentage = 30),
        )
    }
}
