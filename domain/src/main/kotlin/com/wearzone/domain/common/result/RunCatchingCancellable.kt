package com.wearzone.domain.common.result

import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> runCatchingCancellable(block: suspend () -> T): Result<T> =
    runCatching { block() }.also { result ->
        result.exceptionOrNull()?.let { exception ->
            if (exception is CancellationException) throw exception
        }
    }
