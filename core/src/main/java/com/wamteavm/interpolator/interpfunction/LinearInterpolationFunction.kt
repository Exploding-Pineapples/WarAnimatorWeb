package com.wamteavm.interpolator.interpfunction

class LinearInterpolationFunction<I : Number>(x: Array<I>, y: DoubleArray) : InterpolationFunction<I, Double>(x, y.toTypedArray()) {
    override fun evaluate(at: I): Double {
        val atDouble = at.toDouble()

        if (atDouble <= i[0].toDouble()) {
            return o[0]
        }
        if (atDouble >= i[i.size - 1].toDouble()) {
            return o[o.size - 1]
        }

        var index = 0
        while (atDouble > i[index + 1].toDouble()) {
            index++
        }

        val x0 = i[index].toDouble()
        val x1 = i[index + 1].toDouble()
        val y0 = o[index]
        val y1 = o[index + 1]

        return y0 + (y1 - y0) * (atDouble - x0) / (x1 - x0)
    }

    override fun init() {
    }
}
