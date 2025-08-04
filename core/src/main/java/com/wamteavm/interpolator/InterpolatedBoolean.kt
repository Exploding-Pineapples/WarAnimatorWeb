package com.wamteavm.interpolator

import com.wamteavm.interpolator.interpfunction.InterpolationFunction
import com.wamteavm.interpolator.interpfunction.StepInterpolationFunction

class InterpolatedBoolean(initValue: Boolean, initTime: Int) : InterpolatedValue<Int, Boolean>(initValue, initTime) {

    @Transient
    override var interpolationFunction: InterpolationFunction<Int, Boolean> =
        StepInterpolationFunction(
            arrayOf(initTime),
            arrayOf(initValue)
        )

    init {
        setPoints[initTime] = initValue
        updateInterpolationFunction()
    }

    override fun updateInterpolationFunction() {
        interpolationFunction =
            StepInterpolationFunction(
                setPoints.keys.toTypedArray(),
                setPoints.values.toTypedArray()
            )
    }
}
