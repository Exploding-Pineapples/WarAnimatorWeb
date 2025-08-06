package com.wamteavm.models

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.inputelements.InputElement
import com.wamteavm.inputelements.TextInput
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.absoluteValue

@Serializable
data class Node(
    override var position: Coordinate,
    val initTime: Int,
    override val id: NodeID
) : AnyObject, HasScreenPosition, Clickable, HasInputs, HasID {
    override var screenPosition = Coordinate(0f, 0f)
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()
    @Transient var visitedBy = mutableListOf<NodeCollectionID>()
    var tSetPoint: Double? = null
    var edges = mutableListOf<Edge>()

    override fun buildInputs() {
        super.buildInputs()
        inputElements.add(TextInput(null, { input ->
            tSetPoint = input
        }, label@{
            return@label tSetPoint.toString()
        }, Double::class.java, "Set t set point"))
    }

    override fun showInputs(verticalGroup: VerticalGroup, uiVisitor: UIVisitor) {
        uiVisitor.show(verticalGroup, this)
    }

    override fun clicked(x: Float, y: Float): Boolean
    {
        if (screenPosition == null) {
            return false
        }
        return (x - screenPosition.x).absoluteValue <= 10 && (y - screenPosition.y).absoluteValue <= 10
    }

    override fun init() { // Initialize transient properties
        edges.forEach { it.init() }

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
                it.init()
            }
        }
    }
}
