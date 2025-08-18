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

    override fun holdPositionUntil(time: Int) {
        println("Warning: attempt to hold node position as HasPosition")
    }

    fun holdPositionUntil(time: Int, animation: Animation): Node { // Holds ONLY the node, not any of its edges
        val new = Node(position, time, NodeID(animation.nodeId)).apply { this@apply.tSetPoint = this@Node.tSetPoint }
        animation.nodeEdgeHandler.addNode(new)
        return new
    }
}
