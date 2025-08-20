package com.wamteavm.models

import com.wamteavm.interpolator.ColorSetPointInterpolator
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.interpolator.HasSetPoints
import kotlin.math.hypot

interface AnyObject

interface Drawable {
    var order: String

    fun draw(drawer: Drawer)
}

interface InterpolatedObject : AnyObject {
    val initTime: Int

    fun update(time: Int)
}

interface HasPosition {
    var position: Coordinate
    val posInterpolator: HasSetPoints<Int, Coordinate>
}

interface HasInterpolatedPosition : HasPosition, InterpolatedObject {
    override val posInterpolator: CoordinateSetPointInterpolator

    fun init() {
        posInterpolator.updateInterpolationFunction()
    }

    override fun update(time: Int) {
        position = posInterpolator.evaluate(time)
    }

    fun holdPositionUntil(time: Int) {  // Create a new movement that keeps the object at its last defined position until the current time
        posInterpolator.holdValueUntil(time)
    }

    fun removeFrame(time: Int): Boolean {
        return posInterpolator.removeFrame(time)
    }
}

abstract class ScreenObject : HasInterpolatedPosition, Clickable, HasAlpha {
    override fun init() {
        super<HasInterpolatedPosition>.init()
        super<HasAlpha>.init()
    }

    override fun update(time: Int) {
        super<HasInterpolatedPosition>.update(time)
        super<HasAlpha>.update(time)
    }

    override fun clicked(x: Float, y: Float, zoom: Float): Boolean
    {
        return hypot(x - position.x, y - position.y) <= (10 / zoom)
    }

    override fun toString(): String {
        return "Movements: " + posInterpolator.setPoints.keys + "\n" +
            "Positions: " + posInterpolator.setPoints.values + "\n"
    }
}

interface HasID {
    val id: ID
}

interface HasZoom : HasInputs {
    var zoomInterpolator: FloatSetPointInterpolator

    fun init() {
        zoomInterpolator.updateInterpolationFunction()
    }

    fun update(time: Int) {
        zoomInterpolator.evaluate(time)
    }
}

interface HasAlpha : HasInputs {
    val alpha: FloatSetPointInterpolator

    fun init() {
        alpha.updateInterpolationFunction()
    }

    fun update(time: Int) {
        alpha.evaluate(time)
    }
}

interface HasColor : HasInputs {
    var color: ColorSetPointInterpolator

    fun init() {
        color.updateInterpolationFunction()
    }

    fun update(time: Int) {
        color.evaluate(time)
    }
}

interface Clickable {
    fun clicked(x: Float, y: Float, zoom: Float) : Boolean
}
