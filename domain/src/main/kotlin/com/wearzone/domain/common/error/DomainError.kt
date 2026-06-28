package com.wearzone.domain.common.error

sealed interface DomainError {
    data class Network(val exception: Throwable) : DomainError
    data class Server(val code: Int, val message: String?) : DomainError
    data class Unknown(val exception: Throwable) : DomainError
}
