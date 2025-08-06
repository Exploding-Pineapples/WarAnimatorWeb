package com.wamteavm.models

import com.wamteavm.inputelements.TextInput
import com.wamteavm.WarAnimator.DISPLAY_HEIGHT
import com.wamteavm.WarAnimator.DISPLAY_WIDTH
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.utilities.AreaColor
import kotlin.math.absoluteValue

interface AnyObject {
    fun init()
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

    fun goToTime(time: Int): Boolean { // Can only be called after at least one key frame has been added
        position = posInterpolator.evaluate(time)
        return true
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

    fun shouldDraw(time: Int): Boolean
}

abstract class ScreenObject : InterpolatedObject, HasScreenPosition, Clickable, HasInputs {
    @Transient override var screenPosition: Coordinate = Coordinate(0f, 0f)

    override fun clicked(x: Float, y: Float): Boolean
    {
        return (x - screenPosition.x).absoluteValue <= 10 && (y - screenPosition.y).absoluteValue <= 10
    }

    protected open fun goToTime(time: Int, zoom: Float, cx: Float, cy: Float): Boolean {
        super.goToTime(time)
        updateScreenPosition(zoom, cx, cy)
        return shouldDraw(time)
    }

    override fun shouldDraw(time: Int): Boolean {
        return time >= posInterpolator.setPoints.keys.first()
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

    override fun buildInputs() {
        alpha.updateInterpolationFunction()

        inputElements.add(TextInput(null, { input ->
            if (input != null) {
                alpha.newSetPoint(0, input) //TODO get the time
            }
        }, label@{
            return@label alpha.value.toString()
        }, Float::class.java, "Set alpha set point"))
    }
}

interface HasColor : HasInputs {
    var color: AreaColor

    override fun buildInputs() {
        inputElements.add(TextInput(null, { input ->
            if (input != null) {
                for (color in AreaColor.entries) {
                    if (input == color.name) {
                        this.color = color
                    }
                }
            }
        }, label@{
            return@label color.name
        }, String::class.java, "Set color"))
    }
}

interface Clickable {
    fun clicked(x: Float, y: Float) : Boolean
}

