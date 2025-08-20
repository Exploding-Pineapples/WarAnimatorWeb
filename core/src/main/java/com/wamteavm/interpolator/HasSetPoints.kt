package com.wamteavm.interpolator

import com.wamteavm.utilities.SerializableTreeMap

interface HasSetPoints<I : Comparable<I>, O> {
    val setPoints: SerializableTreeMap<I, O>

    fun removeFrame(x: I): Boolean {
        if (setPoints.size > 1) {
            if (setPoints.remove(x) != null) { // Remove was successful or not
                return true
            }
        }
        return false
    }

    fun newSetPoint(time: I, value: O) {
        setPoints[time] = value
    }

    fun newSetPoint(time: I, value: O, removeDuplicates: Boolean) {
        newSetPoint(time, value)

        if (removeDuplicates) { // Remove all set points after the new set point that have the same value in a row
            var found = false
            for (definedTime in setPoints.keys) {
                if (definedTime >= time) {
                    if (found) { // If the last set point already exists
                        if (setPoints[definedTime] == value) {
                            setPoints.remove(definedTime)
                        } else {
                            return
                        }
                    } else {
                        found = true
                    }
                }
            }
        }
    }
}
