package com.wamteavm.models

import com.wamteavm.WarAnimator.DISPLAY_HEIGHT
import com.wamteavm.WarAnimator.DISPLAY_WIDTH
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.utilities.AreaColor
import kotlin.math.absoluteValue

interface AnyObject {
    fun init()
}

interface Drawable {
    var order: String

    fun draw(drawer: Drawer)
}

interface HasPosition {
    var position: Coordinate
}

interface HasScreenPosition : HasPosition {
    var screenPosition: Coordinate

    fun updateScreenPosition(zoom: Float, cx: Float, cy: Float) {
        if (screenPosition == null) { // Is null when animation is first opened because screenPosition is @Transient
            screenPosition = Coordinate(0f, 0f)
        }

        screenPosition.x = position.x * zoom - cx * (zoom - 1) + (DISPLAY_WIDTH / 2 - cx)
        screenPosition.y = position.y * zoom - cy * (zoom - 1) + (DISPLAY_HEIGHT / 2 - cy)
    }
}

interface InterpolatedObject : AnyObject, HasPosition {
    val initTime: Int
    val posInterpolator: CoordinateSetPointInterpolator

    override fun init() {
        posInterpolator.updateInterpolationFunction()
    }

    fun goToTime(time: Int) { // Can only be called after at least one key frame has been added
        position = posInterpolator.evaluate(time)
    }

    fun holdPositionUntil(time: Int) {  // Create a new movement that keeps the object at its last defined position until the current time
        posInterpolator.holdValueUntil(time)
    }

    fun removeFrame(time: Int): Boolean {
        return posInterpolator.removeFrame(time)
    }

    fun newSetPoint(time: Int, x: Float, y: Float) {
        posInterpolator.newSetPoint(time, Coordinate(x, y))
    }
}

abstract class ScreenObject : InterpolatedObject, HasScreenPosition, Clickable {
    @Transient override var screenPosition: Coordinate = Coordinate(0f, 0f)

    override fun clicked(x: Float, y: Float): Boolean
    {
        return (x - screenPosition.x).absoluteValue <= 10 && (y - screenPosition.y).absoluteValue <= 10
    }

    protected open fun goToTime(time: Int, zoom: Float, cx: Float, cy: Float) {
        super.goToTime(time)
        updateScreenPosition(zoom, cx, cy)
    }

    override fun toString(): String {
        return "Movements: " + posInterpolator.setPoints.keys + "\n" +
               "Positions: " + posInterpolator.setPoints.values + "\n"
    }
}

interface HasID {
    val id: ID
}

interface HasZoom {
    var zoomInterpolator: FloatSetPointInterpolator
}

interface HasAlpha : HasInputs {
    val alpha: FloatSetPointInterpolator
}

interface HasColor : HasInputs {
    var color: AreaColor
}

interface Clickable {
    fun clicked(x: Float, y: Float) : Boolean
}

