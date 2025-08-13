package com.wamteavm.loaders.externalloaders

import com.badlogic.gdx.graphics.Texture
import com.wamteavm.models.Animation
import com.waranimator.api.client.WarAnimatorAPI

object APIExternalLoader : AbstractExternalLoader {
    val api = WarAnimatorAPI("https://api.waranimator.com")
    override val animations = mutableListOf<Animation>()
    override val loadedImages: MutableMap<String, Texture> = mutableMapOf()

    init {
        BrowserIO.initHiddenFileInput()
    }

    override fun save() {
        animations.forEach {
            println("saving: " + it.id)
            api.saveAnimation(it.id, it.name, it, Animation.serializer())
        }
        println("saved")
    }

    override fun loadAnimations(callback: () -> Unit) {
        animations.clear()
        api.getMyAnimations().forEach {
            val result = runCatching {
                animations += it.decompressData(Animation.serializer())
            }
            if (result.isFailure) {
                println(result.exceptionOrNull()?.message)
            }
        }
        println("animations loaded")
    }

    override fun addAnimation(animation: Animation) {
        animation.id = api.createAnimation(animation.name, animation, Animation.serializer(), false).id
        animations.add(animation)
    }

    override fun deleteAnimation(animation: Animation) {
        animations.removeIf { it.id == animation.id }
        api.deleteAnimation(animation.id)
        println(animation.name)
    }

    override fun loadImages(animation: Animation) {
        animation.imageKeys.forEach {
            TODO("load image from API")
        }
    }

    override fun addImage() {
        TODO("Not yet implemented")
    }

    override fun exit() {
        if (!api.isLoggedIn()) api.logout()
    }
}
