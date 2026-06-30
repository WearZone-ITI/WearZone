package com.example.wearzone.domain.product.usecase

import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.model.ProductDetail
import com.example.wearzone.domain.product.repository.IProductRepository

class GetProductDetailUseCase(
    private val repository: IProductRepository,
) {
    suspend operator fun invoke(productId: Long): DataResult<ProductDetail> =
        repository.getProductDetail(productId)
}
