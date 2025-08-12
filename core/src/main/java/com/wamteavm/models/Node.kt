package com.wamteavm.models

import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Node(
    override var position: Coordinate,
    override val initTime: Int,
    override val id: NodeID
) : ScreenObject(), HasInputs, HasID {
    @Transient var visitedBy = mutableListOf<NodeCollectionID>()
    var tSetPoint: Double? = null
    var edges = mutableListOf<Edge>()
    override val posInterpolator = CoordinateSetPointInterpolator().apply { interpolated = false }

    override fun update(time: Int) { // Goes to time, and if animation mode is active, draws colored circle
        visitedBy.clear() // Clear to prepare to be traversed
    }
}
