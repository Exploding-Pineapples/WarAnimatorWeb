package com.wamteavm.loaders.externalloaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.GdxRuntimeException
import com.wamteavm.models.Animation
import kotlinx.serialization.json.Json
import java.io.File
import javax.swing.JFileChooser

object DesktopExternalLoader : AbstractExternalLoader {
    override val animations = mutableListOf<Animation>()
    override val loadedImages: MutableMap<String, Texture> = mutableMapOf()
    val json: Json = Json { ignoreUnknownKeys = true }
    val chooser: JFileChooser by lazy {
        JFileChooser(File(Gdx.files.localStoragePath))
    }

    private val animationsFolder by lazy {
        val file = File("animations")
        if (!file.exists()) {
            file.mkdirs()
        }
        file
    }

    override fun save() {
        animations.forEach {
            it.imageKeys = loadedImages.keys.toList()
            val file = File("$animationsFolder","${it.name}.json")

            if (!file.exists()) {
                file.createNewFile()
            }

            kotlin.runCatching {
                file.writeText(json.encodeToString(Animation.serializer(), it))
            }.onFailure { x ->
                x.printStackTrace()
                println("couldnt save ${it.name} rip")
            }
        }
    }

    override fun loadAnimations(callback: () -> Unit) {
        animationsFolder.list()?.forEach { name ->
            if (!animations.any { animation -> animation.name == name.removeSuffix(".json") }) {
                val content = File(animationsFolder.toString(), name).readText()
                kotlin.runCatching {
                    animations += json.decodeFromString<Animation>(content)
                }.onFailure { e ->
                    e.printStackTrace()
                }
            }
        }
        callback()
    }

    override fun loadImages(animation: Animation, callback: () -> Unit) {
        animation.imageKeys.forEach {
            try {
                loadedImages[it] = Texture(Gdx.files.absolute(it))
            } catch (e: GdxRuntimeException) {
                println("File not found")
            }
        }
        callback()
    }

    override fun addImage() {
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            loadedImages[chooser.selectedFile.absolutePath] = (Texture(Gdx.files.absolute(chooser.selectedFile.absolutePath)))
        }
    }

    override fun addAnimation(animation: Animation) {
        animations.add(animation)
        save()
    }

    override fun deleteAnimation(animation: Animation) {
        animations.remove(animation)
        println(animationsFolder.toString())
        val file = File(animationsFolder, "${animation.name}.json")
        if (file.exists()) {
            println(file.delete())
        }
    }

    fun listChildren(parentPath: String): Array<String> {
        val assetsTxtHandle = Gdx.files.internal("assets.txt")
        if (!assetsTxtHandle.exists()) {
            Gdx.app.error("AssetLister", "assets.txt not found in internal files.")
        }

        val normalizedPath = parentPath.trimEnd('/') + "/"

        val out = Array<String>()
        for (string in assetsTxtHandle.readString()
            .lineSequence()
            .map { it.trim() }
            .filter { it.startsWith(normalizedPath) && it.removePrefix(normalizedPath).contains('/').not() }
        )
        {
            out.add(string.removePrefix(normalizedPath))
        }
        return out
    }

    override fun exit() {
    }
}
