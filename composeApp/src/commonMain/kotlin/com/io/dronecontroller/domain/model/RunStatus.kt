package com.io.dronecontroller.domain.model

sealed class RunStatus<out T> {
    data class Success<T>(
        val data: T,
    ) : RunStatus<T>()

    data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : RunStatus<Nothing>()

    object Loading : RunStatus<Nothing>()
}
