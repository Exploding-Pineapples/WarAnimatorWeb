package com.wamteavm.loaders.externalloaders;


import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;

@JSFunctor
interface ImageCallback : JSObject {
    fun onLoad(images : JSArray<Entry>)
}
