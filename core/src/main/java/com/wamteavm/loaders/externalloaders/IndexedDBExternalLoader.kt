package com.wamteavm.loaders.externalloaders

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Base64Coder
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
            override fun onLoad(entries: JSArray<Entry>) {
                for (i in 0 until entries.getLength()) {
                    val entry = entries.get(i)
                    val key = entry.getKey()
                    val value = entry.getValue()

                    val decodedString = String(decodeBase64ToBytes(value), Charsets.UTF_8)
                    addAnimation(json.decodeFromString<Animation>(decodedString).apply { name = key })
                }
                callback()
            }
        })
    }

    override fun save() {
        animations.forEach { animation ->
            animation.imageKeys = loadedImages.keys.toList()
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
        TODO("Not yet implemented")
    }

    override fun loadImages(animation: Animation) {
        BrowserIO.loadIndexedDB( "horsInfo","images", object : ImageCallback {
            override fun onLoad(images: JSArray<Entry>) {
                for (i in 0 until images.getLength()) {
                    val entry = images.get(i)
                    val key = entry.getKey()
                    val value = entry.getValue()

                    if (key in animation.imageKeys) {
                        loadedImages[key] = base64ToTexture(value)
                    }
                }
                animation.images.forEach { it.loadTexture(this@IndexedDBExternalLoader) }
            }
        })

    }

    override fun addImage() {
        BrowserIO.pickImage()
    }
    fun addImage(name: String, base64URL: String) { // Gets called through JS by addImage()
        val base64String = base64URL.split(",")[1]
        loadedImages[name] = base64ToTexture(base64String)

        BrowserIO.saveBase64ToIndexedDB("horsInfo","images", name, base64String)
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
