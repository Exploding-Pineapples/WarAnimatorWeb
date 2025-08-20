package com.wamteavm.models

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

