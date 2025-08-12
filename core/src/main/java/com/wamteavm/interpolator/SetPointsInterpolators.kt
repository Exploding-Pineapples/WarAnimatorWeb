package com.wamteavm.interpolator

import com.badlogic.gdx.graphics.Color
import com.wamteavm.interpolator.interpfunction.LinearInterpolationFunction
import com.wamteavm.interpolator.interpfunction.PCHIPInterpolationFunction
import com.wamteavm.models.Animation
import com.wamteavm.models.Coordinate
import com.wamteavm.models.NodeCollectionSetPoint
import com.wamteavm.screens.AnimationScreen
import com.wamteavm.utilities.ColorWrapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.round

@Serializable
class CoordinateSetPointInterpolator : SetPointInterpolator<Int, Coordinate, Coordinate> {
    override var setPoints: MutableMap<Int, Coordinate> = mutableMapOf()
    @Transient var xInterpolationFunction = PCHIPInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    @Transient var yInterpolationFunction = PCHIPInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    @Transient override var value = Coordinate(0f, 0f)
    override var interpolated: Boolean = true

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

    override fun evaluateWithInterpolator(at: Int): Coordinate {
        value = Coordinate(xInterpolationFunction.evaluate(at).toFloat(), yInterpolationFunction.evaluate(at).toFloat())
        return value
    }
}

@Serializable
class FloatSetPointInterpolator : SetPointInterpolator<Int, Float, Float> {
    override var setPoints: MutableMap<Int, Float> = sortedMapOf()
    override var value: Float = 1f
    @Transient var interpolationFunction = PCHIPInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    override var interpolated: Boolean = true

    override fun updateInterpolationFunction() {
        super.updateInterpolationFunction()
        interpolationFunction = PCHIPInterpolationFunction(setPoints.keys.toTypedArray(), setPoints.values.toTypedArray().map { it.toDouble() }.toDoubleArray())
    }

    override fun evaluateWithInterpolator(at: Int): Float {
        value = interpolationFunction.evaluate(at).toFloat()
        return value
    }
}

@Serializable
class ColorSetPointInterpolator : SetPointInterpolator<Int, ColorWrapper, Color> {
    override var setPoints: MutableMap<Int, ColorWrapper> = sortedMapOf()
    @Transient override var value: Color = Color.BLACK
    override var interpolated: Boolean = true
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

    override fun evaluateWithInterpolator(at: Int): Color {
        value = Color(
            rInterpolationFunction.evaluate(at).toFloat(),
            gInterpolationFunction.evaluate(at).toFloat(),
            bInterpolationFunction.evaluate(at).toFloat(),
            1f
        )
        return value
    }
}

class NodeCollectionInterpolator : SetPointInterpolator<Int, NodeCollectionSetPoint, FloatArray> {
    override var setPoints: MutableMap<Int, NodeCollectionSetPoint> = sortedMapOf()
    override var value: FloatArray = floatArrayOf()
    override var interpolated: Boolean = true
    private var cachedInterpolators: MutableMap<Double, Pair<PCHIPInterpolationFunction<Int>, PCHIPInterpolationFunction<Int>>> = hashMapOf()
    private var zoom: Float = 1f

    override fun updateInterpolationFunction() {
        super.updateInterpolationFunction()

        setPoints.values.forEach { it.updateInterpolators() }
        cachedInterpolators.clear()
    }

    fun prepare(zoom: Float) {
        this.zoom = zoom
    }

    override fun evaluateWithInterpolator(at: Int): FloatArray {
        var num = 0
        var setPoint: NodeCollectionSetPoint? = null

        if (setPoints.isNotEmpty()) {
            if (setPoints.containsKey(at)) {
                setPoint = setPoints[at]!!
                num = round((setPoint.length) / AnimationScreen.LINE_RESOLUTION).toInt()
            } else {
                if (setPoints.size < 2 || at < setPoints.keys.first()) {
                    setPoint = setPoints.values.first()
                    num = round((setPoint.length / AnimationScreen.LINE_RESOLUTION)).toInt()
                } else {
                    var index = 0
                    var time0 = 0
                    var length0 = 0.0
                    var found = false

                    for (frame in setPoints) {
                        if (found) {
                            setPoint = frame.value
                            val deltaTime = setPoint.time - time0
                            num = round((length0 + ((at - time0).toDouble() / deltaTime) * (setPoint.length - length0)) / AnimationScreen.LINE_RESOLUTION).toInt()
                            break
                        }
                        if (frame.key < at) {
                            time0 = frame.value.time
                            length0 = frame.value.length
                            found = true
                        }
                        index++
                    }
                }
            }

            num = (num * zoom).toInt().coerceIn(0..AnimationScreen.MAX_LINES_PER_LENGTH * setPoint!!.length.toInt())

            value = FloatArray(num * 2)
            var parameter = 0.0

            for (i in 0 until num) { // For every point to draw, build an interpolator for the point which evaluates from a specific t (parameter) value (0 to 1) through time
                val cachedInterpolators = this.cachedInterpolators[parameter]
                lateinit var xInterpolatorTime: PCHIPInterpolationFunction<Int>
                lateinit var yInterpolatorTime: PCHIPInterpolationFunction<Int>
                if (cachedInterpolators != null) {
                    xInterpolatorTime = cachedInterpolators.first
                    yInterpolatorTime = cachedInterpolators.second
                } else {
                    //println("creating new")
                    val xInTime = DoubleArray(setPoints.size)
                    val yInTime = DoubleArray(setPoints.size)

                    var index = 0
                    for (frame in setPoints) {
                        // frame.value's interpolators are through space at a specific time
                        xInTime[index] = frame.value.xInterpolator.evaluate(parameter)
                        yInTime[index] = frame.value.yInterpolator.evaluate(parameter)
                        index++
                    }

                    val times = setPoints.keys.toTypedArray()
                    xInterpolatorTime = PCHIPInterpolationFunction(times, xInTime)
                    yInterpolatorTime = PCHIPInterpolationFunction(times, yInTime)

                    this.cachedInterpolators[parameter] = Pair(xInterpolatorTime, yInterpolatorTime)
                }
                value[i * 2] = (xInterpolatorTime.evaluate(at).toFloat())
                value[i * 2 + 1] = (yInterpolatorTime.evaluate(at).toFloat())

                parameter = setPoint.distanceInterpolator.evaluate(((i + 1.0) / num) * setPoint.length)
                //print("$parameter ")
            }
            //println()
        }

        return value
    }

    fun holdValueUntil(time: Int, animation: Animation) { // Special hold function for NodeCollectionInterpolator since values are objects (pointers), not literals
        var prevTime: Int? = null
        var prevValue: NodeCollectionSetPoint? = null

        val frameTimes = setPoints.keys.toList()

        for (i in frameTimes.indices) {
            val definedTime = frameTimes[i]

            if (definedTime == time) {
                return
            }

            if ((definedTime > time) && (prevTime != null)) {
                setPoints[time] = prevValue!!.duplicate(time, animation)
                this.updateInterpolationFunction()

                println("Added hold frame: $setPoints")
                return
            }

            prevTime = definedTime
            prevValue = setPoints[prevTime]
        }

        setPoints[time] = setPoints.values.last().duplicate(time, animation)
        this.updateInterpolationFunction()
        print(setPoints)
    }
}
