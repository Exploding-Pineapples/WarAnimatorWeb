package com.wamteavm.models

import com.badlogic.gdx.graphics.OrthographicCamera
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.absoluteValue

@Serializable
data class Node(
    override var position: Coordinate,
    val initTime: Int,
    override val id: NodeID
) : AnyObject, HasScreenPosition, Clickable, HasInputs, HasID {
    @Transient override var screenPosition = Coordinate(0f, 0f)
    @Transient var visitedBy = mutableListOf<NodeCollectionID>()
    var tSetPoint: Double? = null
    var edges = mutableListOf<Edge>()

    override fun clicked(x: Float, y: Float): Boolean
    {
        if (screenPosition == null) {
            return false
        }
        return (x - screenPosition.x).absoluteValue <= 10 && (y - screenPosition.y).absoluteValue <= 10
    }

    override fun init() { // Initialize transient properties
        edges.forEach { it.prepare() }

        if (visitedBy == null) {
            visitedBy = mutableListOf()
        }

        if (screenPosition == null) {
            screenPosition = Coordinate(0f, 0f)
        }
    }

    fun update(time: Int, camera: OrthographicCamera) { // Goes to time, and if animation mode is active, draws colored circle
        visitedBy.clear() // Clear to prepare to be traversed
        updateScreenPosition(camera.zoom, camera.position.x, camera.position.y)
        if (time == initTime) {
            edges.forEach {
                it.prepare()
            }
        }
    }
}
