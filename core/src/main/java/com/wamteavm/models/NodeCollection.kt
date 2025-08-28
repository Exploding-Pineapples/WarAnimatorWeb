package com.wamteavm.models

import com.wamteavm.interpolator.ColorSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.interpolator.NodeCollectionInterpolator
import com.wamteavm.utilities.clickedCoordinates
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
open class NodeCollection(override val id: NodeCollectionID) : AnyObject, HasInputs, HasID, HasAlpha, HasColor,
    Clickable, Drawable {
    override var order = "e"
    override var alpha: FloatSetPointInterpolator = FloatSetPointInterpolator()
    @Transient var interpolator: NodeCollectionInterpolator = NodeCollectionInterpolator()
    override var color: ColorSetPointInterpolator = ColorSetPointInterpolator()

    var type: String = "None"
    var width: Float? = null

    fun getSetPointOfNode(node: Node, time: Int): NodeCollectionSetPoint? {
        val setPoints = interpolator.setPoints[time]
        return setPoints?.firstOrNull { it.nodes.contains(node) }
    }

    fun contains(node: Node): Boolean {
        return node.posInterpolator.setPoints.keys.any { time ->
            interpolator.setPoints[time]?.any { it.contains(node) } == true
        }
    }

    fun duplicateAt(time: Int, animation: Animation) {
        interpolator.holdValueUntil(time, animation)
    }

    fun deleteAt(time: Int, animation: Animation) {
        interpolator.setPoints[time]?.forEach {
            it.deleteAt(time, animation)
        }
    }

    override fun init() {
        super<HasAlpha>.init()
        super<HasColor>.init()
        interpolator.updateInterpolationFunction()
    }

    override fun update(time: Int) {
        super<HasAlpha>.update(time)
        super<HasColor>.update(time)
        interpolator.evaluate(time)
    }

    fun update(time: Int, zoom: Float) {
        interpolator.prepare(zoom)
        update(time)
    }

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }

    override fun clicked(x: Float, y: Float, zoom: Float): Boolean {
        return interpolator.value.any { clickedCoordinates(x, y, zoom, it) }
    }
}
