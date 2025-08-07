package com.wamteavm.models
import com.wamteavm.utilities.clickedCoordinates
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Edge(
    var collectionID: NodeCollectionID,
    var segment: Pair<NodeID, NodeID>,
    @Transient var screenCoords: MutableList<Coordinate> = mutableListOf(),
) : AnyObject, Clickable {

    override fun init() {
        prepare()
    }

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


