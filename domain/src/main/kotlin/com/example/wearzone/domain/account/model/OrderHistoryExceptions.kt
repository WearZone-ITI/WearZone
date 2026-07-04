package com.example.wearzone.domain.account.model

class OrderHistoryNetworkException : Exception()
class OrderHistoryPermissionException : Exception()
class OrderHistoryResponseParseException : Exception()
class OrderHistoryCustomerNotFoundException : Exception()
class OrderHistoryUnknownException(cause: Throwable) : Exception(cause)

class OrderDetailsNotFoundException : Exception()
class OrderDetailsOwnershipMismatchException : Exception()
class OrderCancelNotAllowedException : Exception()
class OrderCancelFailedException(cause: Throwable? = null) : Exception(cause)
