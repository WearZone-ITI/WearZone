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
            CouponOffer(code = "BUY4GET25", discountPercentage = 25),
            CouponOffer(code="CODE_DISCOUNT_BLACKFRIDAY", discountPercentage = 80),

        )
    }
}
