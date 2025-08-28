package com.wamteavm.models
import com.wamteavm.utilities.clickedCoordinates
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Edge(
    var collectionID: NodeCollectionID,
    var segment: Pair<NodeID, NodeID>,
    val times: MutableSet<Int>,
    @Transient var coords: MutableList<Coordinate> = mutableListOf(),
) : AnyObject, Clickable {
    override fun clicked(x: Float, y: Float, zoom: Float): Boolean {
        return clickedCoordinates(x, y, zoom, coords.toTypedArray())
    }

    fun updateCoords(animation: Animation) {
        val first = animation.getNodeByID(segment.first)?.position
        val second = animation.getNodeByID(segment.second)?.position
        coords = if (first != null && second != null) {
            mutableListOf(first, second)
        } else {
            mutableListOf()
        }
    }

    override fun toString(): String {
        return "Edge of collection ${collectionID.value} from node ${segment.first.value} to node ${segment.second.value}"
    }

    fun duplicateAt(time: Int) {
        times.add(time)
    }
}
