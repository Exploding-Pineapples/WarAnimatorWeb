package com.wamteavm.models

import com.wamteavm.WarAnimator.DISPLAY_HEIGHT
import com.wamteavm.WarAnimator.DISPLAY_WIDTH
import kotlinx.serialization.Serializable

interface HasInputs

@Serializable
data class Coordinate(
    var x: Float,
    var y: Float
)

interface ID : Comparable<ID> {
    val value: Int

    override fun compareTo(other: ID): Int {
        return value - other.value
    }

    fun duplicate() : ID
}

@Serializable
class NodeCollectionID(override val value: Int = -1) : ID {
    override fun duplicate() : NodeCollectionID {
        return NodeCollectionID(value)
    }
}

@Serializable
class NodeID(override val value: Int = -1) : ID {
    override fun duplicate(): NodeID {
        return NodeID(value)
    }
}

fun projectToScreen(position: Coordinate, zoom: Float, cx: Float, cy: Float): Coordinate {
    return Coordinate(
        position.x * zoom - cx * (zoom - 1) + (DISPLAY_WIDTH / 2 - cx),
        position.y * zoom - cy * (zoom - 1) + (DISPLAY_HEIGHT / 2 - cy)
    )
}
