package com.wamteavm.loaders.externalloaders

import com.badlogic.gdx.graphics.Texture
import com.wamteavm.models.Animation

interface AbstractExternalLoader {
    val animations: MutableList<Animation>
    val loadedImages: MutableMap<String, Texture>

    fun saveAnimations()
    fun loadAnimations(callback: () -> Unit = {})
    fun addAnimation(animation: Animation)
    fun deleteAnimation(animation: Animation)
    fun loadImages(animation: Animation, callback: () -> Unit = {})
    fun addImage(animation: Animation)
    fun deleteImage(key: String) {
        loadedImages.remove(key)
    }
    fun exit()
}
