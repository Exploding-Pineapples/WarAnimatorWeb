package com.wamteavm.interpolator

interface SetPointInterpolator<I : Comparable<I>, O, V> : HasSetPoints<I, O> { // Number to interpolate over, set point type, actual output type (usually the same as O but sometimes different due to some processing)
    var value: V
    var interpolated: Boolean

    fun updateInterpolationFunction()

    override fun newSetPoint(time: I, value: O) {
        super.newSetPoint(time, value)
        updateInterpolationFunction()
    }

    override fun newSetPoint(time: I, value: O, removeDuplicates: Boolean) {
        super.newSetPoint(time, value, removeDuplicates)
        updateInterpolationFunction()
    }

    override fun removeFrame(x: I): Boolean {
        if (super.removeFrame(x)) {
            updateInterpolationFunction()
            return true
        }
        return false
    }

    // When you add a time coordinate pair to an object which hasn't had a defined movement for a long time, it will interpolate a motion the whole way, which can be undesirable
    // Ex. last defined position was at time 0, you want it to move to another position at 800
    // But you only want it to move starting from time 600
    // The below function is used hold the object at the last position until the desired time
    fun holdValueUntil(time: I) {
        var prevTime: I? = null
        var prevValue: O? = null

        val frameTimes = setPoints.keys.toList()

        for (i in frameTimes.indices) {
            val definedTime = frameTimes[i]

            if (definedTime == time) { // If the time is already defined, don't do anything
                return
            }

            if ((definedTime > time) && (prevTime != null)) { // If the input time is not defined but is in the defined period, modify the movement to stay at the position just before the input time until the input time
                setPoints[time] = prevValue!!
                updateInterpolationFunction()

                println("Added hold frame: $setPoints")
                return
            }

            prevTime = definedTime
            prevValue = setPoints[prevTime]
        }
        // If the input time was not in the defined period, add a movement to the end
        setPoints[time] = setPoints.values.last()

        updateInterpolationFunction()
    }

    fun evaluateWithInterpolator(at: I): V

    fun evaluate(at: I): V {
        if (interpolated) { value = evaluateWithInterpolator(at) }
        return value
    }
}
