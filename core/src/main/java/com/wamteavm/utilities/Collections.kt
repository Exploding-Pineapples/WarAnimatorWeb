package com.wamteavm.utilities

import com.badlogic.gdx.utils.Array

fun <T>gdxArrayOf(list: Collection<T>): Array<T> {
    return Array<T>().apply { list.forEach { add(it) } }
}
fun <T>gdxArrayOf(list: Array<T>): Array<T> {
    return Array<T>().apply { list.forEach { add(it) } }
}
