package com.wamteavm.loaders.externalloaders

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Base64Coder
import com.wamteavm.loaders.externalloaders.browserio.BrowserIO
import com.wamteavm.loaders.externalloaders.browserio.Entry
import com.wamteavm.loaders.externalloaders.browserio.ImageCallback
import com.wamteavm.models.Animation
import kotlinx.serialization.json.Json
import org.teavm.jso.JSBody
import org.teavm.jso.core.JSArray


object IndexedDBExternalLoader : AbstractExternalLoader {
    override val loadedImages: MutableMap<String, Texture> = mutableMapOf()
    override val animations = mutableListOf<Animation>()
    val json: Json = Json { ignoreUnknownKeys = true }

    init {
        BrowserIO.initHiddenFileInput()
        BrowserIO.openDatabase("horsInfo", 1, arrayOf("images", "animations"))
    }

    override fun loadAnimations(callback: () -> Unit) {
        BrowserIO.loadIndexedDB( "horsInfo","animations", object : ImageCallback {
            override fun onLoad(images: JSArray<Entry>) {
                for (i in 0 until images.length) {
                    val entry = images.get(i)
                    val key = entry.getKey()
                    val value = entry.getValue()

                    val decodedString = String(decodeBase64ToBytes(value), Charsets.UTF_8)
                    addAnimation(json.decodeFromString<Animation>(decodedString).apply { name = key })
                }
                callback()
            }
        })
    }

    override fun saveAnimations() {
        animations.forEach { animation ->
            val jsonString = json.encodeToString(Animation.serializer(), animation)
            val base64String = java.util.Base64.getEncoder()
                .encodeToString(jsonString.toByteArray(Charsets.UTF_8))
            BrowserIO.saveBase64ToIndexedDB("horsInfo", "animations", animation.name, base64String)
        }
    }

    override fun addAnimation(animation: Animation) {
        animations.add(animation)
    }

    override fun deleteAnimation(animation: Animation) {
        animations.remove(animation)
    }

    override fun addImage(animation: Animation) {
        BrowserIO.pickImage( object: ImageCallback {
            override fun onLoad(images: JSArray<Entry>) {
                for (i in 0 until images.length) {
                    val entry = images.get(i)
                    val key = entry.getKey()
                    val value = entry.getValue().split(",")[1]
                    loadedImages[key] = base64ToTexture(value)
                    animation.imageKeys.add(key)
                    BrowserIO.saveBase64ToIndexedDB("horsInfo","images", key, value)
                }
            }
        })
    }

    override fun loadImages(animation: Animation, callback: () -> Unit) {
        loadedImages.clear()
        BrowserIO.loadIndexedDB( "horsInfo","images", object : ImageCallback {
            override fun onLoad(images: JSArray<Entry>) {
                for (i in 0 until images.length) {
                    val entry = images.get(i)
                    val key = entry.getKey()
                    val value = entry.getValue()

                    if (key in animation.imageKeys) {
                        loadedImages[key] = base64ToTexture(value)
                    }
                }
                animation.loadExternal(this@IndexedDBExternalLoader)
                callback()
            }
        })
    }

    fun base64ToTexture(base64String: String): Texture {
        // Convert Base64 -> Pixmap -> Texture
        val imageBytes = Base64Coder.decode(base64String)
        val pixmap = Pixmap(imageBytes, 0, imageBytes.size)
        return Texture(pixmap)
    }

    override fun exit() {
    }
}

@JSBody(params = ["b64"], script = """
    b64 = String(b64).trim();
    return Uint8Array.from(atob(b64), c => c.charCodeAt(0));
""")
external fun decodeBase64ToBytes(b64: String): ByteArray
