package com.wearzone.data.remote.interceptor

import java.io.IOException

sealed class ShopifyHttpException(
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)

class UnauthorizedException(cause: Throwable? = null) :
    ShopifyHttpException("401 Unauthorized - token invalid or missing", cause)

class ForbiddenException(cause: Throwable? = null) :
    ShopifyHttpException("403 Forbidden - insufficient scope", cause)

class NotFoundException(cause: Throwable? = null) :
    ShopifyHttpException("404 Not Found", cause)

class ValidationException(body: String?, cause: Throwable? = null) :
    ShopifyHttpException("422 Unprocessable: $body", cause)

class ServerException(code: Int, cause: Throwable? = null) :
    ShopifyHttpException("5xx Server Error: $code", cause)
