package com.example.wearzone.domain.product.usecase

import com.example.wearzone.domain.common.runCatchingCancellable
import com.example.wearzone.domain.product.model.ProductDetail
import com.example.wearzone.domain.product.repository.IProductRepository

class GetProductDetailUseCase(
    private val repository: IProductRepository,
) {
    suspend operator fun invoke(productId: Long): Result<ProductDetail> =
        repository.getProductDetail(productId)
}
