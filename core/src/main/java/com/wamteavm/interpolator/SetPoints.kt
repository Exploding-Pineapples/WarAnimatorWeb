package com.wamteavm.interpolator

import com.badlogic.gdx.graphics.Color
import com.wamteavm.interpolator.interpfunction.LinearInterpolationFunction
import com.wamteavm.interpolator.interpfunction.PCHIPInterpolationFunction
import com.wamteavm.models.Coordinate
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class CoordinateSetPoints : HasSetPoints<Int, Coordinate, Coordinate> {
    override var setPoints: MutableMap<Int, Coordinate> = mutableMapOf()
    @Transient var xInterpolationFunction = PCHIPInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    @Transient var yInterpolationFunction = PCHIPInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    @Transient override var value = Coordinate(0f, 0f)

    override fun updateInterpolationFunction() {
        super.updateInterpolationFunction()
        val ts = setPoints.keys.toTypedArray()
        val xs = DoubleArray(setPoints.size)
        val ys = DoubleArray(setPoints.size)
        for ((i, coordinate) in setPoints.values.withIndex()) {
            xs[i] = coordinate.x.toDouble()
            ys[i] = coordinate.y.toDouble()
        }
        xInterpolationFunction = PCHIPInterpolationFunction(ts, xs)
        yInterpolationFunction = PCHIPInterpolationFunction(ts, ys)
    }

    override fun evaluate(at: Int): Coordinate {
        value = Coordinate(xInterpolationFunction.evaluate(at).toFloat(), yInterpolationFunction.evaluate(at).toFloat())
        return value
    }
}

@Serializable
class FloatSetPoints : HasSetPoints<Int, Float, Float> {
    override var setPoints: MutableMap<Int, Float> = sortedMapOf()
    override var value: Float = 1f
    @Transient var zoomInterpolationFunction = PCHIPInterpolationFunction<Int>(arrayOf(), doubleArrayOf())

    override fun updateInterpolationFunction() {
        super.updateInterpolationFunction()
        zoomInterpolationFunction = PCHIPInterpolationFunction(setPoints.keys.toTypedArray(), setPoints.values.toTypedArray().map { it.toDouble() }.toDoubleArray())
    }

    override fun evaluate(at: Int): Float {
        value = zoomInterpolationFunction.evaluate(at).toFloat()
        return value
    }
}

@Serializable
class ColorSetPoints : HasSetPoints<Int, AreaColor, Color> {
    override var setPoints: MutableMap<Int, AreaColor> = sortedMapOf()
    @Transient override var value: Color = Color.BLACK
    @Transient var rInterpolationFunction = LinearInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    @Transient var gInterpolationFunction = LinearInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    @Transient var bInterpolationFunction = LinearInterpolationFunction<Int>(arrayOf(), doubleArrayOf())

    override fun updateInterpolationFunction() {
        super.updateInterpolationFunction()

        val ts = setPoints.keys.toTypedArray()
        val rs = DoubleArray(setPoints.keys.size)
        val gs = DoubleArray(setPoints.keys.size)
        val bs = DoubleArray(setPoints.keys.size)

        for ((i, color) in setPoints.values.withIndex()) {
            rs[i] = color.color.r.toDouble()
            gs[i] = color.color.g.toDouble()
            bs[i] = color.color.b.toDouble()
        }

        rInterpolationFunction = LinearInterpolationFunction(ts, rs)
        gInterpolationFunction = LinearInterpolationFunction(ts, gs)
        bInterpolationFunction = LinearInterpolationFunction(ts, bs)
    }

    override fun evaluate(at: Int): Color {
        value = Color(
            rInterpolationFunction.evaluate(at).toFloat(),
            gInterpolationFunction.evaluate(at).toFloat(),
            bInterpolationFunction.evaluate(at).toFloat(),
            1f
        )
        return value
    }
}
