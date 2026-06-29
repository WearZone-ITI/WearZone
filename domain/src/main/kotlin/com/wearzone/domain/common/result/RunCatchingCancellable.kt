package com.wearzone.domain.common.result

import kotlin.coroutines.cancellation.CancellationException

/**
 * A safe alternative to [runCatching] for coroutines.
 * It catches all exceptions from the [block] and returns them as a [Result.failure],
 * EXCEPT for [CancellationException], which is re-thrown so the coroutine can cancel properly.
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    runCatching { block() }.also { result ->
        result.exceptionOrNull()?.let { e ->
            if (e is CancellationException) throw e
        }
    }
