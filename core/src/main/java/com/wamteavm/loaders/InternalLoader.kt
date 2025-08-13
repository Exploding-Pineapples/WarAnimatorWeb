package com.wamteavm.loaders

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Array
import java.util.*

object InternalLoader {
    private val LOADED_TEXTURES: MutableMap<String, Texture> = HashMap()
    private val LOADED_SKINS: MutableMap<String, Skin> = HashMap()

    fun loadSkin(file: String): Skin? {
        val cached = LOADED_SKINS[file]
        if (cached != null) {
            return cached
        }

        try {
            val skin = Skin(Gdx.files.internal(file))
            println("skin loaded")
            return skin
        } catch (e: Exception) {
            Gdx.app.error("UI", "Failed to load skin", e)
        }

        return null
    }

    fun loadTexture(internalPath: String): Texture? {
        val cached = LOADED_TEXTURES[internalPath]
        if (cached != null) {
            return cached
        }

        try {
            val texture = Texture(Gdx.files.internal(internalPath))
            LOADED_TEXTURES[internalPath] = texture
            return texture
        } catch (e: RuntimeException) {
            return null
        }
    }

    fun whitePixel(): TextureRegion {
        return TextureRegion(Texture(Gdx.files.internal("habibi.png")))
    }

    fun loadFont(): BitmapFont {
        val texture = Texture(Gdx.files.internal("fonts/bitstream_vera_sans/distancefield.png"), true)
        texture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear)
        return BitmapFont(
            Gdx.files.internal("fonts/bitstream_vera_sans/distancefield.fnt"),
            TextureRegion(texture),
            false
        )
    }

    fun loadFontShader(): ShaderProgram {
        return ShaderProgram(
            Gdx.files.internal("fonts/bitstream_vera_sans/font.vert"),
            Gdx.files.internal("fonts/bitstream_vera_sans/font.frag")
        )
    }


}
