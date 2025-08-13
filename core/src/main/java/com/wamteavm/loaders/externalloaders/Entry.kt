package com.wamteavm.loaders.externalloaders;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

interface Entry : JSObject {
    @JSProperty("key")
    fun getKey() : String

    @JSProperty("value")
    fun getValue() : String
}
