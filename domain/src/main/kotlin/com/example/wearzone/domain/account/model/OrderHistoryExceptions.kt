package com.example.wearzone.domain.account.model

class OrderHistoryNetworkException : Exception()
class OrderHistoryPermissionException : Exception()
class OrderHistoryResponseParseException : Exception()
class OrderHistoryCustomerNotFoundException : Exception()
class OrderHistoryUnknownException(cause: Throwable) : Exception(cause)
