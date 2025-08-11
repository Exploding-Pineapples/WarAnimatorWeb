package com.wamteavm.models.screenobjects

import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.*
import com.wamteavm.utilities.ColorWrapper
import kotlinx.serialization.Serializable

@Serializable
class Label(override var position: Coordinate, override var initTime: Int) : ScreenObject(), HasAlpha, HasColor, Drawable {
    override var order = "b"
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override val alpha: FloatSetPointInterpolator = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    var text = ""
    override var color: ColorWrapper = ColorWrapper(1f, 0f, 0f, 1f)
    var size = 1f

    override fun init() {
        super.init()
        alpha.updateInterpolationFunction()
    }

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }

    fun goToTime(time: Int, zoom: Float, cx: Float, cy: Float, paused: Boolean) {
        if (!paused) { alpha.evaluate(time) }
        super.goToTime(time, zoom, cx, cy)
    }
}
