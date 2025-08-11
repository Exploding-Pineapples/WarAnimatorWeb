package com.wamteavm.models

import com.wamteavm.WarAnimator
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import kotlinx.serialization.Serializable

@Serializable
data class Camera(
    override var position: Coordinate = Coordinate(WarAnimator.DISPLAY_WIDTH / 2f, WarAnimator.DISPLAY_HEIGHT / 2f),
    override val initTime: Int
) : ScreenObject(), HasZoom {
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override var zoomInterpolator: FloatSetPointInterpolator = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }

    override fun init() {
        super.init()
        zoomInterpolator.updateInterpolationFunction()
    }

    override fun goToTime(time: Int) {
        zoomInterpolator.evaluate(time)
        super.goToTime(time, zoomInterpolator.value, position.x, position.y)
    }

    override fun holdPositionUntil(time: Int) {  // Create a new movement that keeps the object at its last defined position until the current time
        super.holdPositionUntil(time)
        zoomInterpolator.holdValueUntil(time)
    } //TODO separate position and zoom holds

    override fun removeFrame(time: Int): Boolean {
        val zoomResult = zoomInterpolator.removeFrame(time)
        val positionResult = super.removeFrame(time)
        return zoomResult || positionResult // If either a zoom or position frame is removed it is a success
    }


}
