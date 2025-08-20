package com.wamteavm.models.screenobjects

import com.wamteavm.interpolator.ColorSetPointInterpolator
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.*
import com.wamteavm.utilities.ColorWrapper
import kotlinx.serialization.Serializable

@Serializable
class Arrow(override var position: Coordinate, override var initTime: Int): ScreenObject(), HasColor, Drawable {
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override val alpha: FloatSetPointInterpolator = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    override var color: ColorSetPointInterpolator = ColorSetPointInterpolator().apply { newSetPoint(initTime, ColorWrapper.parseString("black")!!) }
    override var order = "f"

    override fun init() {
        super<ScreenObject>.init()
        super<HasColor>.init()
    }

    override fun update(time: Int) {
        super<ScreenObject>.update(time)
        super<HasColor>.update(time)
    }

    var thickness = 10f

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }
}
