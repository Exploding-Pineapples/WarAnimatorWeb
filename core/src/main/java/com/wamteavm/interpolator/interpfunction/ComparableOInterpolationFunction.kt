package com.wamteavm.interpolator.interpfunction

abstract class ComparableOInterpolationFunction<I : Number, O : Comparable<O>>(i: Array<I>, o: Array<O>) : InterpolationFunction<I, O>(i, o) {
    fun inRange(oVal: O): Boolean {
        return (oVal >= o.first() && oVal <= o.last())
    }
}
