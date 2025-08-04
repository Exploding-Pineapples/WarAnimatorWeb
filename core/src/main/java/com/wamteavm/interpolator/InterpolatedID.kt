package com.wamteavm.interpolator

import com.wamteavm.interpolator.interpfunction.InterpolationFunction
import com.wamteavm.interpolator.interpfunction.StepInterpolationFunction


class InterpolatedID(initTime: Int, initID: Int) : InterpolatedValue<Int, Int>(initID, initTime) {
    @Transient
    override var interpolationFunction: InterpolationFunction<Int, Int> =
        StepInterpolationFunction(
            arrayOf(initTime),
            arrayOf(initID)
        )

    init {
        println("Created new Interpolated Value")
        setPoints[initTime] = initID
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
