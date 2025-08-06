package com.wamteavm.models.screenobjects

import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.ui.inputelements.InputElement
import com.wamteavm.ui.inputelements.TextInput
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.*
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class MapLabel(override var position: Coordinate, override var initTime: Int) : ScreenObject(), HasAlpha, HasColor {
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override val alpha: FloatSetPointInterpolator = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    var text = ""
    override var color = AreaColor.RED
    var size = 1f
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()

    override fun init() {
        super.init()
        alpha.updateInterpolationFunction()
    }

    fun goToTime(time: Int, zoom: Float, cx: Float, cy: Float, paused: Boolean): Boolean {
        if (!paused) { alpha.evaluate(time) }
        return super.goToTime(time, zoom, cx, cy)
    }

    override fun buildInputs() {
        super<ScreenObject>.buildInputs()
        super<HasColor>.buildInputs()
        super<HasAlpha>.buildInputs()
    }
}
