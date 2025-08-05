package com.wamteavm.files

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.wamteavm.models.Animation

object FileHandler {

    val animations = mutableListOf<Animation>()

    private val animationsFolder by lazy {
        val file = Gdx.files.local("animations")
        if (!file.exists()) {
            file.mkdirs()
        }
        file
    }

    fun deleteAnimation(animation: Animation) {
        animations.remove(animation)
        val file = FileHandle("$animationsFolder/${animation.name}")
        if (file.exists()) {
            file.delete()
            println("deleted")
        }
    }

    fun save() {
        animations.forEach {
            val fileName = it.name
            val file = FileHandle("$animationsFolder/$fileName")

            if (!file.exists()) {
                file.file().createNewFile()
            }

            println("placeholder save")
        }
    }

    fun createNewAnimation(animation: Animation) {
        animations.add(animation)
    }

    fun load() {
        println("placeholder loading system")

        animationsFolder.list().forEach {
            val content = it.read()
            animations += Animation(it.name())
        }
        println("animations loaded")
    }
}
