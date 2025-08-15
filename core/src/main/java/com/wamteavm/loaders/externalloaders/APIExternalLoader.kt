package com.wamteavm.loaders.externalloaders

import com.badlogic.gdx.graphics.Texture
import com.wamteavm.loaders.externalloaders.IndexedDBExternalLoader.base64ToTexture
import com.wamteavm.models.Animation
import com.waranimator.api.client.WarAnimatorAPI
import org.teavm.jso.core.JSArray

object APIExternalLoader : AbstractExternalLoader {
    val api = WarAnimatorAPI("https://api.waranimator.com")
    override val animations = mutableListOf<Animation>()
    override val loadedImages: MutableMap<String, Texture> = mutableMapOf()

    override fun saveAnimations() {
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

    override fun addImage() {
        BrowserIO.pickImage( object: ImageCallback {
            override fun onLoad(images: JSArray<Entry>) {
                for (i in 0 until images.length) {
                    val entry = images.get(i)
                    val key = entry.getKey()
                    val value = entry.getValue().split(",")[1]
                    loadedImages[key] = base64ToTexture(value)
                    //TODO api.saveImage(key, value)
                }
            }
        })
    }

    override fun loadImages(animation: Animation) {
        animation.imageKeys.forEach {
            TODO("load image from API")
        }
    }

    override fun exit() {
        if (api.isLoggedIn()) api.logout()
    }
}
