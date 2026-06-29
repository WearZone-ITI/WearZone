package com.example.wearzone.domain.common

import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> runCatchingCancellable(block: suspend () -> T): Result<T> =
    runCatching { block() }.also { result ->
        result.exceptionOrNull()?.let { e ->
            if (e is CancellationException) throw e
        }
    }
