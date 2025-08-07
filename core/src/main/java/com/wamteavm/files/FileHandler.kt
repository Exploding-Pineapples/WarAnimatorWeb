package com.wamteavm.files

import com.wamteavm.models.Animation
import kotlinx.serialization.json.Json

import java.io.File

object FileHandler {
    val json = Json { ignoreUnknownKeys = true }
    val animations = mutableListOf<Animation>()

    private val animationsFolder by lazy {
        val file = File("animations")
        if (!file.exists()) {
            file.mkdirs()
        }
        file
    }

    fun deleteAnimation(animation: Animation) {
        animations.remove(animation)
        println(animation.name)
        val file = File(animationsFolder.toString(), "${animation.name}.json")
        if (file.exists()) {
            println("deleted: ${file.delete()}")
        }
    }

    fun save() {
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
        println("saved")
    }

    fun addAnimation(animation: Animation) {
        animations.add(animation)
    }

    fun load() {
        animationsFolder.list()?.forEach { name ->
            if (!animations.any { animation -> animation.name == name.removeSuffix(".json") })
            {
                val content = File(animationsFolder.toString(), name).readText()
                kotlin.runCatching {
                    animations += json.decodeFromString<Animation>(content)
                }.onFailure { e ->
                    e.printStackTrace()
                }
            }
        }
        println("animations loaded")
    }
}
