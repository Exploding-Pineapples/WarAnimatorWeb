package com.wamteavm.interpolator.interpfunction

class StepInterpolationFunction<I : Number, O>(x: Array<I>, y: Array<O>) :
    InterpolationFunction<I, O>(x, y) {
    override fun evaluate(at: I): O {
        val atDouble = at.toDouble()
        if (atDouble <= i.first().toDouble()) {
            return o[0]
        }

        if (atDouble >= i.last().toDouble()) {
            return o.last()
        }

        for ((index, definedTime) in i.withIndex()) {
            if (definedTime.toDouble() > atDouble) {
                return o[index - 1]
            }
        }
        throw IllegalStateException("Step interpolation failed somehow")
    }
}
