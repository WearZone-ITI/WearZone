package com.example.wearzone.domain.common

sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val error: DomainError) : DataResult<Nothing>
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> {
    return when (this) {
        is DataResult.Success -> DataResult.Success(transform(data))
        is DataResult.Error -> DataResult.Error(error)
    }
}
