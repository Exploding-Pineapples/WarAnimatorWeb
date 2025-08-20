package com.wamteavm.interpolator

import com.wamteavm.interpolator.interpfunction.LinearInterpolationFunction
import com.wamteavm.interpolator.interpfunction.PCHIPInterpolationFunction
import com.wamteavm.models.Animation
import com.wamteavm.models.Coordinate
import com.wamteavm.models.NodeCollectionSetPoint
import com.wamteavm.screens.AnimationScreen
import com.wamteavm.utilities.ColorWrapper
import com.wamteavm.utilities.SerializableTreeMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.min

@Serializable
class CoordinateSetPointInterpolator : SetPointInterpolator<Int, Coordinate, Coordinate> {
    override var setPoints: SerializableTreeMap<Int, Coordinate> = SerializableTreeMap()
    @Transient var xInterpolationFunction = PCHIPInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    @Transient var yInterpolationFunction = PCHIPInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    @Transient override var value = Coordinate(0f, 0f)
    override var interpolated: Boolean = true

    override fun updateInterpolationFunction() {
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
    override var setPoints: SerializableTreeMap<Int, Float> = SerializableTreeMap()
    override var value: Float = 1f
    @Transient var interpolationFunction = PCHIPInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    override var interpolated: Boolean = true

    override fun updateInterpolationFunction() {
                interpolationFunction = PCHIPInterpolationFunction(setPoints.keys.toTypedArray(), setPoints.values.toTypedArray().map { it.toDouble() }.toDoubleArray())
    }

    override fun evaluateWithInterpolator(at: Int): Float {
        value = interpolationFunction.evaluate(at).toFloat()
        return value
    }
}

@Serializable
class ColorSetPointInterpolator : SetPointInterpolator<Int, ColorWrapper, ColorWrapper> {
    override var setPoints: SerializableTreeMap<Int, ColorWrapper> = SerializableTreeMap()
    override var value: ColorWrapper = ColorWrapper.parseString("red")!!
    override var interpolated: Boolean = true
    @Transient var rInterpolationFunction = LinearInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    @Transient var gInterpolationFunction = LinearInterpolationFunction<Int>(arrayOf(), doubleArrayOf())
    @Transient var bInterpolationFunction = LinearInterpolationFunction<Int>(arrayOf(), doubleArrayOf())

    override fun updateInterpolationFunction() {
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

    override fun evaluateWithInterpolator(at: Int): ColorWrapper {
        value = ColorWrapper(
            rInterpolationFunction.evaluate(at).toFloat(),
            gInterpolationFunction.evaluate(at).toFloat(),
            bInterpolationFunction.evaluate(at).toFloat(),
            1f
        )
        return value
    }
}

class NodeCollectionInterpolator : SetPointInterpolator<Int, MutableList<NodeCollectionSetPoint>, Array<FloatArray>> {
    override var setPoints: SerializableTreeMap<Int, MutableList<NodeCollectionSetPoint>> = SerializableTreeMap()
    override var value: Array<FloatArray> = arrayOf()
    override var interpolated: Boolean = true
    private var cachedInterpolators: MutableMap<Double, Pair<PCHIPInterpolationFunction<Int>, PCHIPInterpolationFunction<Int>>> = hashMapOf()
    private var zoom: Float = 1f

    override fun updateInterpolationFunction() {
        setPoints.values.forEach { setPoints1 -> setPoints1.forEach { it.updateInterpolators() } }
        cachedInterpolators.clear()
    }

    fun prepare(zoom: Float) {
        this.zoom = zoom
    }

    fun holdValueUntil(time: Int, animation: Animation) {
        if (setPoints.isNotEmpty()) {
            if (setPoints.size == 1) {
                setPoints.values.first().forEach {
                    it.duplicateAt(time, animation)
                }
                return
            }
            if (time > setPoints.keys.last()) {
                setPoints.values.last().forEach {
                    it.duplicateAt(time, animation)
                }
            }
            var prev: Int? = null
            for (definedTime in setPoints.keys) {
                if (definedTime > time) {
                    if (prev != null) {
                        setPoints[prev]!!.forEach {
                            it.duplicateAt(time, animation)
                        }
                    }
                }
                prev = definedTime
            }
        }
        animation.nodeEdgeHandler.updateNodeCollections()
    }

    override fun evaluateWithInterpolator(at: Int): Array<FloatArray> { // at is a time
        val nums = mutableListOf<Int>()
        var foundSetPoints: MutableList<NodeCollectionSetPoint> = mutableListOf()

        if (setPoints.isNotEmpty()) {
            if (setPoints.containsKey(at)) {
                foundSetPoints = setPoints[at]!!
                for (setPoint in foundSetPoints) {
                    nums += (setPoint.length / AnimationScreen.LINE_RESOLUTION).toInt()
                }
            } else {
                if (setPoints.size == 1 || at < setPoints.keys.first()) {
                    foundSetPoints = setPoints.values.first()
                    for (setPoint in foundSetPoints) {
                        nums += (setPoint.length / AnimationScreen.LINE_RESOLUTION).toInt()
                    }
                } else {
                    lateinit var prev: MutableList<NodeCollectionSetPoint>

                    for (frame in setPoints) {
                        if (frame.key > at) {
                            val time0 = prev.first().time
                            val length0s = mutableListOf<Double>()
                            for (setPoint in prev) {
                                length0s += (setPoint.length) / AnimationScreen.LINE_RESOLUTION
                            }
                            foundSetPoints = frame.value
                            val deltaTime = foundSetPoints.first().time - time0
                            val lesserLength = min(foundSetPoints.size, length0s.size)
                            for (index in 0 until lesserLength) {
                                val setPoint = foundSetPoints[index]
                                val length1 = setPoint.length / AnimationScreen.LINE_RESOLUTION
                                nums += (length0s[index] + ((at - time0).toDouble() / deltaTime) * (length1 - length0s[index])).toInt()
                            }
                            if (foundSetPoints.size > length0s.size) {
                                for (index in lesserLength until foundSetPoints.size) {
                                    val setPoint = foundSetPoints[index]
                                    val length1 = setPoint.length / AnimationScreen.LINE_RESOLUTION
                                    nums += length1.toInt()
                                }
                            }
                            if (length0s.size > foundSetPoints.size) {
                                for (index in lesserLength until length0s.size) {
                                    nums += length0s[index].toInt()
                                }
                            }

                            break
                        }
                        prev = frame.value
                    }
                }
            }

            for (i in foundSetPoints.indices) {
                nums[i] = (nums[i] * zoom).toInt().coerceIn(0..AnimationScreen.MAX_LINES_PER_LENGTH * foundSetPoints[i].length.toInt())
            }

            value = Array(foundSetPoints.size) { setPointIndex: Int -> FloatArray(nums[setPointIndex] * 2) }
            var parameter: Double

            for ((setPointIndex, num) in nums.withIndex()) {
                for (i in 0 until num) { // For every point to draw, build an interpolator for the point which evaluates from a specific t (parameter) value (0 to 1) through time
                    val setPoint = foundSetPoints[setPointIndex]
                    parameter = setPoint.distanceInterpolator.evaluate((i.toDouble() / nums[setPointIndex]) * setPoint.length)
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
                            val interpolators: NodeCollectionSetPoint = if (setPointIndex > frame.value.size - 1) {
                                frame.value.first { it.tInterpolator.inRange(parameter) }
                            } else {
                                frame.value[setPointIndex]
                            }
                            xInTime[index] = interpolators.xInterpolator.evaluate(parameter)
                            yInTime[index] = interpolators.yInterpolator.evaluate(parameter)
                            index++
                        }

                        val times = setPoints.keys.toTypedArray()
                        xInterpolatorTime = PCHIPInterpolationFunction(times, xInTime)
                        yInterpolatorTime = PCHIPInterpolationFunction(times, yInTime)

                        this.cachedInterpolators[parameter] = Pair(xInterpolatorTime, yInterpolatorTime)
                    }
                    value[setPointIndex][i * 2] = xInterpolatorTime.evaluate(at).toFloat()
                    value[setPointIndex][i * 2 + 1] = yInterpolatorTime.evaluate(at).toFloat()
                }
            }
        }

        return value
    }
}
