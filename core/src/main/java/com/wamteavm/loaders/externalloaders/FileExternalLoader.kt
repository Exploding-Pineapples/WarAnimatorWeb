package com.wamteavm.loaders.externalloaders

import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.wamteavm.models.Animation
import kotlinx.serialization.json.Json
import java.io.File

object FileExternalLoader : AbstractExternalLoader {
    override val animations = mutableListOf<Animation>()
    override val images: MutableList<Image> = mutableListOf()
    val json: Json = Json { ignoreUnknownKeys = true }

    private val animationsFolder by lazy {
        val file = File("animations")
        if (!file.exists()) {
            file.mkdirs()
        }
        file
    }

    override fun save() {
        animations.forEach {
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

    override fun load() {
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

    override fun exit() {
    }
}
