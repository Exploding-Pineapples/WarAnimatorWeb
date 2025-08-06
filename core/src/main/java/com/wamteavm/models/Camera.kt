package com.wamteavm.models

import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.WarAnimator
import com.wamteavm.inputelements.InputElement
import com.wamteavm.interpolator.CoordinateSetPoints
import com.wamteavm.interpolator.FloatSetPoints
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Camera(
    override var position: Coordinate = Coordinate(WarAnimator.DISPLAY_WIDTH / 2f, WarAnimator.DISPLAY_HEIGHT / 2f),
    override val initTime: Int
) : ScreenObject(), HasZoom {
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()
    override val posSetPoints: CoordinateSetPoints = CoordinateSetPoints().apply { newSetPoint(initTime, Coordinate(0f, 0f)) }
    override var zoomInterpolator: FloatSetPoints = FloatSetPoints().apply { newSetPoint(initTime, 1f) }

    override fun goToTime(time: Int): Boolean {
        zoomInterpolator.evaluate(time)
        super.goToTime(time, zoomInterpolator.value, position.x, position.y)
        return true
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

    override fun showInputs(verticalGroup: VerticalGroup, uiVisitor: UIVisitor) {
        uiVisitor.show(verticalGroup, this)
    }
}
