package com.spliteasy.spliteasy.core

inline fun <T> Result<T>.onSuccessOrNull(block: (T) -> Unit): Throwable? =
    exceptionOrNull().also { if (it == null) getOrNull()?.let(block) }
