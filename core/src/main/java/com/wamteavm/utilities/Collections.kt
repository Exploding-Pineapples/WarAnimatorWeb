package com.wamteavm.utilities

fun <K : Number> Collection<K>.toDoubleArray() = map { it.toDouble() }.toTypedArray()
