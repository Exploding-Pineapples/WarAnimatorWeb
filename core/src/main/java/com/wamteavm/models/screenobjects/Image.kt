package com.wamteavm.models.screenobjects

import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.*
import com.wamteavm.utilities.LoadedTexture
import kotlinx.serialization.Serializable

@Serializable
class Image(override var position: Coordinate, override var initTime: Int) : ScreenObjectWithAlpha(), Drawable {
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override var alpha = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    override var order = "a"

    var scale: Float = 1f
    var texture: LoadedTexture = LoadedTexture()

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }
}
