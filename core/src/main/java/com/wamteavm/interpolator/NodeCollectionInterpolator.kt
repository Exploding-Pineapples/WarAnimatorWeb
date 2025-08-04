package com.wamteavm.interpolator

import com.badlogic.gdx.graphics.OrthographicCamera
import com.wamteavm.WarAnimator.DISPLAY_HEIGHT
import com.wamteavm.WarAnimator.DISPLAY_WIDTH
import com.wamteavm.interpolator.interpfunction.PCHIPInterpolationFunction
import com.wamteavm.models.Animation
import com.wamteavm.models.NodeCollectionSetPoint
import com.wamteavm.screens.AnimationScreen
import java.util.*
import kotlin.math.round

class NodeCollectionInterpolator : HasSetPoints<Int, NodeCollectionSetPoint> {
    override var setPoints: SortedMap<Int, NodeCollectionSetPoint> = TreeMap()
    var coordinates: FloatArray = floatArrayOf()
    var screenCoordinates: FloatArray = floatArrayOf()
    var cachedInterpolators: MutableMap<Double, Pair<PCHIPInterpolationFunction<Int>, PCHIPInterpolationFunction<Int>>> = hashMapOf()

    override fun updateInterpolationFunction() {
        setPoints.values.forEach { it.updateInterpolators() }
        cachedInterpolators.clear()
    }

    fun updateScreenCoordinates(camera: OrthographicCamera) {
        screenCoordinates = FloatArray(coordinates.size)
        for (i in coordinates.indices step 2) { // project like this instead of using projectToScreen() to avoid boxing of Coordinate class
            screenCoordinates[i] = coordinates[i] * camera.zoom - camera.position.x * (camera.zoom - 1) + (DISPLAY_WIDTH / 2 - camera.position.x)
            screenCoordinates[i + 1] = coordinates[i + 1] * camera.zoom - camera.position.y * (camera.zoom - 1) + (DISPLAY_HEIGHT / 2 - camera.position.y)
        }
    }

    fun evaluate(time: Int): FloatArray {
        var num = 0

        if (setPoints.isNotEmpty()) {
            if (setPoints.containsKey(time)) {
                num = round((setPoints[time]?.length ?: 0.0) / AnimationScreen.LINE_RESOLUTION).toInt()
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
                                round((length0 + ((time - time0).toDouble() / deltaTime) * (frame.value.length - length0)) / AnimationScreen.LINE_RESOLUTION).toInt()
                            break
                        }
                        if (frame.key < time) {
                            time0 = frame.value.time
                            length0 = frame.value.length
                            found = true
                        }
                        index++
                    }
                }
            }

            num = num.coerceIn(0..AnimationScreen.MAX_LINES)

            coordinates = FloatArray(num * 2)
            val parameter = DoubleArray(num) { index -> index.toDouble() / num }

            for (i in 0 until num) { // For every point to draw, build an interpolator for the point which evaluates from a specific t (parameter) value (0 to 1) through time
                val cachedInterpolators = this.cachedInterpolators[parameter[i]]
                if (cachedInterpolators != null) {
                    coordinates[i * 2] = (cachedInterpolators.first.evaluate(time).toFloat())
                    coordinates[i * 2 + 1] = (cachedInterpolators.second.evaluate(time).toFloat())
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
                    val xInterpolatorTime = PCHIPInterpolationFunction<Int>(times, xInTime)
                    val yInterpolatorTime = PCHIPInterpolationFunction<Int>(times, yInTime)

                    coordinates[i * 2] = (xInterpolatorTime.evaluate(time).toFloat())
                    coordinates[i * 2 + 1] = (yInterpolatorTime.evaluate(time).toFloat())

                    this.cachedInterpolators[parameter[i]] = Pair(xInterpolatorTime, yInterpolatorTime)
                }
            }
        }

        return coordinates
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
