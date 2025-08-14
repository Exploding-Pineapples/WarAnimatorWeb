package com.wamteavm.utilities

import com.badlogic.gdx.graphics.Texture
import com.wamteavm.loaders.InternalLoader
import com.wamteavm.loaders.externalloaders.AbstractExternalLoader
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class LoadedTexture {
    @Transient
    var loaded = false
    var name: String = ""
    var key: String = ""
        set(new) {
            field = new
            loaded = false
        }
    @Transient
    private var texture: Texture? = null

    fun loadTexture(externalLoader: AbstractExternalLoader?) {
        if (!loaded) {
            texture = if (externalLoader != null) {
                externalLoader.loadedImages[key]
            } else {
                InternalLoader.loadTexture(key)
            }
            loaded = texture != null
        }
    }

    fun getTexture(): Texture? {
        return texture
    }
}
