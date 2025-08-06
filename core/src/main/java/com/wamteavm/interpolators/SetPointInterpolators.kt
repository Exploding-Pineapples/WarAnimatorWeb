package com.wamteavm.interpolators

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.wamteavm.WarAnimator.DISPLAY_HEIGHT
import com.wamteavm.WarAnimator.DISPLAY_WIDTH
import com.wamteavm.interpolators.interpfunction.LinearInterpolationFunction
import com.wamteavm.interpolators.interpfunction.PCHIPInterpolationFunction
import com.wamteavm.models.Animation
import com.wamteavm.models.Coordinate
import com.wamteavm.models.NodeCollectionSetPoint
import com.wamteavm.screens.AnimationScreen
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.round

@Serializable
class CoordinateSetPointInterpolator : SetPointInterpolator<Int, Coordinate, Coordinate> {
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
class FloatSetPointInterpolator : SetPointInterpolator<Int, Float, Float> {
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
class ColorSetPointInterpolator : SetPointInterpolator<Int, AreaColor, Color> {
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

class NodeCollectionInterpolator : SetPointInterpolator<Int, NodeCollectionSetPoint, FloatArray> {
    override var setPoints: MutableMap<Int, NodeCollectionSetPoint> = sortedMapOf()
    override var value: FloatArray = floatArrayOf()
    var screenCoordinates: FloatArray = floatArrayOf()
    var cachedInterpolators: MutableMap<Double, Pair<PCHIPInterpolationFunction<Int>, PCHIPInterpolationFunction<Int>>> = hashMapOf()

    override fun updateInterpolationFunction() {
        super.updateInterpolationFunction()

        setPoints.values.forEach { it.updateInterpolators() }
        cachedInterpolators.clear()
    }

    fun updateScreenCoordinates(camera: OrthographicCamera) {
        screenCoordinates = FloatArray(value.size)
        for (i in value.indices step 2) { // project like this instead of using projectToScreen() to avoid boxing of Coordinate class
            screenCoordinates[i] = value[i] * camera.zoom - camera.position.x * (camera.zoom - 1) + (DISPLAY_WIDTH / 2 - camera.position.x)
            screenCoordinates[i + 1] = value[i + 1] * camera.zoom - camera.position.y * (camera.zoom - 1) + (DISPLAY_HEIGHT / 2 - camera.position.y)
        }
    }

    override fun evaluate(at: Int): FloatArray {
        var num = 0

        if (setPoints.isNotEmpty()) {
            if (setPoints.containsKey(at)) {
                num = round((setPoints[at]?.length ?: 0.0) / AnimationScreen.LINE_RESOLUTION).toInt()
            } else {
                if (setPoints.size < 2) {
                    num = round((setPoints.values.first().length / AnimationScreen.LINE_RESOLUTION)).toInt()
                } else {
                    var index = 0
                    var time0 = 0
                    var length0 = 0.0
                    var found = false

                    for (frame in setPoints) {
                        if (found) {
                            val deltaTime = frame.value.time - time0
                            num =
                                round((length0 + ((at - time0).toDouble() / deltaTime) * (frame.value.length - length0)) / AnimationScreen.LINE_RESOLUTION).toInt()
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

            num = num.coerceIn(0..AnimationScreen.MAX_LINES)

            value = FloatArray(num * 2)
            val parameter = DoubleArray(num) { index -> index.toDouble() / num }

            for (i in 0 until num) { // For every point to draw, build an interpolator for the point which evaluates from a specific t (parameter) value (0 to 1) through time
                val cachedInterpolators = this.cachedInterpolators[parameter[i]]
                if (cachedInterpolators != null) {
                    value[i * 2] = (cachedInterpolators.first.evaluate(at).toFloat())
                    value[i * 2 + 1] = (cachedInterpolators.second.evaluate(at).toFloat())
                } else {
                    val xInTime = DoubleArray(setPoints.size)
                    val yInTime = DoubleArray(setPoints.size)

                    var index = 0
                    for (frame in setPoints) {
                        // frame.value's interpolators are through space at a specific time
                        xInTime[index] = frame.value.xInterpolator.evaluate(parameter[i])
                        yInTime[index] = frame.value.yInterpolator.evaluate(parameter[i])
                        index++
                    }

                    val times = setPoints.keys.toTypedArray()
                    val xInterpolatorTime = PCHIPInterpolationFunction(times, xInTime)
                    val yInterpolatorTime = PCHIPInterpolationFunction(times, yInTime)

                    value[i * 2] = (xInterpolatorTime.evaluate(at).toFloat())
                    value[i * 2 + 1] = (yInterpolatorTime.evaluate(at).toFloat())

                    this.cachedInterpolators[parameter[i]] = Pair(xInterpolatorTime, yInterpolatorTime)
                }
            }
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
