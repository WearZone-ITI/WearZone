package com.example.wearzone.domain.product.usecase

import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.repository.IProductRepository

class GetProductsByVendorUseCase(
    private val repository: IProductRepository
) {
    suspend operator fun invoke(vendor: String): DataResult<List<Product>> {
        return repository.getProductsByVendor(vendor)
    }
}
