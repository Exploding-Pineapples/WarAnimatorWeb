package com.wamteavm.interpolator

import com.wamteavm.models.Coordinate
import com.wamteavm.utilities.SerializableTreeMap
import kotlinx.serialization.Serializable

@Serializable
class CoordinateSetPoints : HasSetPoints<Int, Coordinate> {
    override val setPoints: SerializableTreeMap<Int, Coordinate> = SerializableTreeMap()
}

@Serializable
class TSetPoints : HasSetPoints<Int, MutableMap<Int, Double>> {
    override val setPoints: SerializableTreeMap<Int, MutableMap<Int, Double>> = SerializableTreeMap()
}
