package com.wamteavm.loaders.externalloaders

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.wamteavm.models.Animation

interface AbstractExternalLoader {
    val animations: MutableList<Animation>
    val images: MutableList<Image>

    fun save()
    fun load()
    fun addAnimation(animation: Animation)
    fun deleteAnimation(animation: Animation)
    fun addImage(image: Image) {
        images.add(image)
    }
    fun deleteImage(image: Image) {
        images.remove(image)
    }
    fun exit()
}
