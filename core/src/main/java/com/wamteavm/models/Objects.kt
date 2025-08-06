package com.wamteavm.models

import com.wamteavm.inputelements.TextInput
import com.wamteavm.WarAnimator.DISPLAY_HEIGHT
import com.wamteavm.WarAnimator.DISPLAY_WIDTH
import com.wamteavm.inputelements.InputElement
import com.wamteavm.interpolators.ColorSetPointInterpolator
import com.wamteavm.interpolators.CoordinateSetPointInterpolator
import com.wamteavm.interpolators.FloatSetPointInterpolator
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.absoluteValue

interface AnyObject {
    fun init()
}

interface Drawable {
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
    val posSetPoints: CoordinateSetPointInterpolator

    override fun init() {
        posSetPoints.updateInterpolationFunction()
    }

    fun update(time: Int): Boolean {
        position = posSetPoints.evaluate(time)
        return true
    }

    fun holdPositionUntil(time: Int) {
        posSetPoints.holdValueUntil(time)
    }

    fun removeFrame(time: Int): Boolean {
        return posSetPoints.removeFrame(time)
    }

    fun newSetPoint(time: Int, x: Float, y: Float) {
        posSetPoints.newSetPoint(time, Coordinate(x, y))
    }
}

abstract class ScreenObject : InterpolatedObject, HasScreenPosition, Clickable, HasInputs {
    @Transient override var screenPosition: Coordinate = Coordinate(0f, 0f)
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()

    override fun init() {
        buildInputs()
    }

    override fun clicked(x: Float, y: Float): Boolean
    {
        return (x - screenPosition.x).absoluteValue <= 10 && (y - screenPosition.y).absoluteValue <= 10
    }

    protected fun update(time: Int, zoom: Float, cx: Float, cy: Float) {
        super.update(time)
        updateScreenPosition(zoom, cx, cy)
    }

    override fun toString(): String {
        return "Movements: " + posSetPoints.setPoints.keys + "\n" +
            "Positions: " + posSetPoints.setPoints.values + "\n"
    }
}

abstract class ScreenObjectWithAlpha : ScreenObject(), HasAlpha, Drawable {
    override var alpha = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    @Transient override var screenPosition: Coordinate = Coordinate(0f, 0f)
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()

    override fun init() {
        buildInputs()
        alpha.updateInterpolationFunction()
    }

    protected open fun update(time: Int, zoom: Float, cx: Float, cy: Float, paused: Boolean) {
        super.update(time, zoom, cx, cy)
        if (!paused) {
            alpha.evaluate(time)
        }
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
                alpha.newSetPoint(0, input) // TODO get the time
            }
        }, label@{
            return@label alpha.value.toString()
        }, Float::class.java, "Set alpha set point"))
    }
}

interface HasColor : HasInputs { // TODO make direct hex color work
    var color: ColorSetPointInterpolator

    override fun buildInputs() {
        inputElements.add(TextInput(null, { input ->
            if (input != null) {
                for (color in AreaColor.entries) {
                    if (input == color.name) {
                        this.color.newSetPoint(0, color)
                        this.color.value = color.color
                    }
                }
            }
        }, label@{
            return@label color.value.toString()
        }, String::class.java, "Set color"))
    }
}

interface Clickable {
    fun clicked(x: Float, y: Float) : Boolean
}

