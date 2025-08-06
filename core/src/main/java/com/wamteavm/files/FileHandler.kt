package com.wamteavm.files

import com.wamteavm.models.Animation
import com.wamteavm.models.AnyObject
import com.wamteavm.models.screenobjects.Arrow
import com.wamteavm.models.screenobjects.Unit
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

import java.io.File



object FileHandler {
    val module = SerializersModule {
        polymorphic(AnyObject::class, AnyObject.serializer()) {
            subclass(Unit::class, Unit.serializer())
            subclass(Arrow::class, Arrow.serializer())
        }
    }

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

            println("saved")
        }
    }

    fun addAnimation(animation: Animation) {
        animations.add(animation)
    }

    fun load() {
        animationsFolder.list()?.forEach { name ->
            val content = File(animationsFolder.toString(), name).readText()

            if (!animations.any { animation -> animation.name == name.removeSuffix(".json") })
            {
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
