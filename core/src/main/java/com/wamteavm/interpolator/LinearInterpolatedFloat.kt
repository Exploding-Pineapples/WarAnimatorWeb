package com.wamteavm.interpolator

import com.wamteavm.interpolator.interpfunction.LinearInterpolationFunction
import com.wamteavm.interpolator.interpfunction.map
import com.wamteavm.utilities.toDoubleArray

class LinearInterpolatedFloat(initValue: Float, initTime: Int) : InterpolatedValue<Int, Float>(initValue, initTime) {

    @Transient
    override var interpolationFunction = LinearInterpolationFunction(
        arrayOf(initTime.toDouble()),
        arrayOf(initValue.toDouble())
    ).map({
        it.toInt()
    }, {
        it.toFloat()
    }, {
        it.toDouble()
    })

    init {
        setPoints[initTime] = initValue
        updateInterpolationFunction()
    }

    override fun updateInterpolationFunction() {
        interpolationFunction = LinearInterpolationFunction(
            setPoints.keys.toDoubleArray(),
            setPoints.values.toDoubleArray()
        ).map({ it.toInt() }, { it.toFloat() }, { it.toDouble() })
    }
}
