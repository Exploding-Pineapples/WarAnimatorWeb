package com.wamteavm.models.screenobjects

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.ui.inputelements.InputElement
import com.wamteavm.ui.inputelements.SelectBoxInput
import com.wamteavm.ui.inputelements.TextInput
import com.wamteavm.files.Assets
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.Coordinate
import com.wamteavm.models.HasAlpha
import com.wamteavm.models.ScreenObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Image(override var position: Coordinate, override var initTime: Int, var path: String) : ScreenObject(),
    HasAlpha {
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override var alpha = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()
    var scale: Float = 1f

    @Transient var texture: Texture? = Assets.loadTexture(path)

    override fun init() {
        super.init()
        buildInputs()
        loadTexture()
        alpha.updateInterpolationFunction()
    }

    fun loadTexture() {
        if (path == "") {
            println("Image path is empty")
        }
        texture = Assets.loadTexture(path)
        println("updated image path to $texture")
    }

    fun updateTexture(newPath: String) {
        path = newPath
        loadTexture()
    }

    fun goToTime(time: Int, zoom: Float, cx: Float, cy: Float, paused: Boolean): Boolean {
        if (!paused) { alpha.evaluate(time) }
        return super.goToTime(time, zoom, cx, cy)
    }

    override fun buildInputs() {
        super<ScreenObject>.buildInputs()
        super<HasAlpha>.buildInputs()
    }
}
