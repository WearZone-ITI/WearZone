package com.example.domain.common.result

import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> runCatchingCancellable(block: suspend () -> T): Result<T> =
    runCatching { block() }.also { result ->
        result.exceptionOrNull()?.let { e ->
            if (e is CancellationException) throw e
        }
    }
