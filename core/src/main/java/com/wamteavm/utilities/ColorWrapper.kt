package com.wamteavm.utilities

import com.badlogic.gdx.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class ColorWrapper(var red: Float, var green: Float, var blue: Float, var alpha: Float) {
    @Transient val color: Color = Color(red, green, blue, alpha)

    companion object {
        fun parseString(input: String): ColorWrapper? {
            var string = input.lowercase()
            when (string) {
                "red" -> return ColorWrapper(1f, 0f, 0f, 1f)
                "orange" -> return ColorWrapper(1f, 0.5f, 0f, 1f)
                "yellow" -> return ColorWrapper(1f, 1f, 0f, 1f)
                "green" -> return ColorWrapper(0f, 1f, 0f, 1f)
                "blue" -> return ColorWrapper(0f, 0f, 1f, 1f)
                "purple" -> return ColorWrapper(.33f, 0f, 1f, 1f)
                "white" -> return ColorWrapper(1f, 1f, 1f, 1f)
                "black" -> return ColorWrapper(0f, 0f, 0f, 1f)
                "gray" -> return ColorWrapper(0.5f, 0.5f, 0.5f, 1f)
                "dark gray" -> return ColorWrapper(0.3f, 0.3f, 0.3f, 1f)
                "light gray" -> return ColorWrapper(0.7f, 0.7f, 0.7f, 1f)
                "pink" -> return ColorWrapper(1f, 0f, 1f, 1f)
            }
            string = string.removePrefix("#")
            try {
                val r = string.substring(0, 2).toInt(16) / 255f
                val g = string.substring(2, 4).toInt(16) / 255f
                val b = string.substring(4, 6).toInt(16) / 255f
                return ColorWrapper(r, g, b, 1f)
            } catch (e: NumberFormatException) {
                return null
            } catch (e: StringIndexOutOfBoundsException) {
                return null
            }
        }
    }
}
