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
        update(time)
        interpolator.prepare(zoom)
    }

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }

    override fun clicked(x: Float, y: Float, zoom: Float): Boolean {
        return interpolator.value.any { clickedCoordinates(x, y, zoom, it) }
    }
}
