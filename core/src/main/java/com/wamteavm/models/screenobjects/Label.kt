package com.wamteavm.models.screenobjects

import com.wamteavm.interpolator.ColorSetPointInterpolator
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.*
import com.wamteavm.utilities.ColorWrapper
import kotlinx.serialization.Serializable
import kotlin.math.hypot

@Serializable
class Label(override var position: Coordinate, override var initTime: Int) : ScreenObjectWithAlpha(), HasColor, Drawable {
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override val alpha: FloatSetPointInterpolator = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    override var color: ColorSetPointInterpolator = ColorSetPointInterpolator().apply { newSetPoint(initTime, ColorWrapper.parseString("red")!!) }
    override var order = "b"

    var text = ""
    var size = 1f

    override fun init() {
        super<ScreenObjectWithAlpha>.init()
        super<HasColor>.init()
    }

    override fun update(time: Int) {
        super<ScreenObjectWithAlpha>.update(time)
        super<HasColor>.update(time)
    }

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }

    override fun clicked(x: Float, y: Float, zoom: Float): Boolean {
        return hypot(x - position.x, y - position.y) <= (size * 10)
    }
}
