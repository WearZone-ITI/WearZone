package com.example.wearzone.domain.product.usecase

import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.repository.IProductRepository

class GetBrandsUseCase(
    private val repository: IProductRepository
) {
    suspend operator fun invoke(): DataResult<List<Brand>> {
        return repository.getBrands()
    }
}
