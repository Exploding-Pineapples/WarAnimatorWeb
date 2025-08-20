package com.wamteavm.models

import com.wamteavm.interpolator.CoordinateSetPoints
import com.wamteavm.interpolator.TSetPoints
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.hypot

@Serializable
data class Node(
    override var position: Coordinate,
    val initTime: Int,
    override val id: NodeID
) : AnyObject, HasPosition, HasInputs, HasID, Clickable {
    @Transient var visitedBy = mutableListOf<Pair<Int, NodeCollectionID>>()
    var tSetPoints: TSetPoints = TSetPoints() // Maps time to mutable map of node collection ID to t value for that node collection
    var edges = mutableListOf<Edge>()
    override val posInterpolator = CoordinateSetPoints().apply { setPoints[initTime] = position }

    fun update(time: Int) {
        val value = posInterpolator.setPoints[time]
        if (value != null) {
            position = value
        }
    }

    fun timeDefined(time: Int): Boolean {
        return posInterpolator.setPoints.containsKey(time)
    }

    fun duplicateAt(time: Int) {
        posInterpolator.setPoints[time] = position
    }

    override fun clicked(x: Float, y: Float, zoom: Float): Boolean
    {
        return hypot(x - position.x, y - position.y) <= (10 / zoom)
    }
}
