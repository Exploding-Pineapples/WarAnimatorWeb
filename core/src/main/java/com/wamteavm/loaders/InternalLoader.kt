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

    var countryNames: Array<String> = Array()
    var images: Array<String> = Array()
    var unitTypes: Array<String> = Array()

    fun flagsPath(file: String): String { return "units/countries/$file" }
    fun unitKindsPath(file: String): String { return "units/symbols/$file" }
    fun mapsPath(file: String): String { return "maps/$file" }

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

    fun countryNames(): Array<String> {
        if (countryNames.isEmpty) {
            updateCountryNames()
        }
        return countryNames
    }

    fun unitTypes(): Array<String> {
        if (unitTypes.isEmpty) {
            updateUnitTypes()
        }
        return unitTypes
    }

    fun images(): Array<String> {
        if (images.isEmpty) {
            updateImages()
        }
        return images
    }

    fun updateCountryNames() {
        countryNames.clear()
        countryNames = listFiles("units/countries")
    }

    fun updateImages() {
        images.clear()
        images = listFiles("maps")
    }

    fun updateUnitTypes() {
        unitTypes.clear()
        unitTypes = listFiles("units/symbols")
    }

    fun listFiles(internalPath: String): Array<String> {
        val fileNames = Array<String>()
        Gdx.files.internal(internalPath).list().forEach {
            print("${it.name()} ")
        }
        println()
        for (file in Gdx.files.internal(internalPath).list()!!) {
            fileNames.add(file.name())
        }
        return fileNames
    }
}
