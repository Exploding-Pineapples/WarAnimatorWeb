package com.wamteavm.models
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.hypot
import kotlin.math.tan

@Serializable
class Edge(
    var collectionID: NodeCollectionID,
    var segment: Pair<NodeID, NodeID>,
    @Transient var screenCoords: MutableList<Coordinate> = mutableListOf(),
) : AnyObject, Clickable {

    override fun clicked(x: Float, y: Float): Boolean {
        return clickedCoordinates(x, y, screenCoords.toTypedArray())
    }

    override fun toString(): String {
        return "Edge of collection ${collectionID.value} from node ${segment.first.value} to node ${segment.second.value}"
    }
    fun contains(nodeID: NodeID): Boolean {
        return  (nodeID.value == segment.first.value || nodeID.value == segment.second.value)
    }
    fun prepare() {
        if (screenCoords == null) {
            screenCoords = mutableListOf()
        }
    }

    fun updateScreenCoords(animation: Animation) {
        screenCoords.clear()
        screenCoords.add(animation.getNodeByID(segment.first)!!.screenPosition)
        screenCoords.add(animation.getNodeByID(segment.second)!!.screenPosition)
    }
}

fun distanceFromPointToSegment(p: Coordinate, a: Coordinate, b: Coordinate): Float {
    if (a.x == b.x) {
        if (a.y == b.y) {
            return hypot((p.x - a.x).toDouble(), (p.x - b.y).toDouble()).toFloat()
        }
        return abs(p.x - a.x)
    }

    val slope = (b.y - a.y) / (b.x - a.x)
    val angle = atan(slope)
    val perp = angle + Math.PI / 2

    val xInt: Double = a.y - p.y + slope * a.x - tan(perp) * p.x
    val yInt: Double = tan(perp) * (xInt - p.x) + p.y

    return hypot((xInt - p.x), (yInt - p.y)).toFloat()
}

fun clickedCoordinates(x: Float, y: Float, coordinates: Array<Coordinate>): Boolean {
    if (coordinates.isNotEmpty()) {
        for (i in 1..coordinates.lastIndex) {
            val dist = distanceFromPointToSegment(
                Coordinate(x, y),
                Coordinate(coordinates[i - 1].x, coordinates[i - 1].y),
                Coordinate(coordinates[i].x, coordinates[i].y),
            )
            if (dist <= 10) {
                return true
            }
        }
    }
    return false
}

fun clickedCoordinates(x: Float, y: Float, coordinates: Array<Float>): Boolean {
    if (coordinates.isNotEmpty()) {
        for (i in 4..coordinates.lastIndex step 2) {
            val dist = distanceFromPointToSegment(
                Coordinate(x, y),
                Coordinate(coordinates[i - 3], coordinates[i - 2]),
                Coordinate(coordinates[i - 1], coordinates[i]),
            )
            if (dist <= 10) {
                return true
            }
        }
    }
    return false
}
