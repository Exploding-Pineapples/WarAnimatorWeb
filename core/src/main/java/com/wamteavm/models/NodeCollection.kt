package com.wamteavm.models

import com.badlogic.gdx.graphics.OrthographicCamera
import com.wamteavm.interpolator.ColorSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.interpolator.NodeCollectionInterpolator
import com.wamteavm.utilities.ColorWrapper
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
        alpha.updateInterpolationFunction()
        interpolator = NodeCollectionInterpolator()
    }

    override fun update(time: Int) {
        super<HasAlpha>.update(time)
        super<HasColor>.update(time)
    }

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }

    fun update(time: Int, camera: OrthographicCamera) {
        update(time)
        interpolator.evaluate(time)
        interpolator.prepare(camera.zoom)
        interpolator.updateScreenCoordinates(camera)
    }

    override fun clicked(x: Float, y: Float, zoom: Float): Boolean {
        return clickedCoordinates(x, y, zoom, interpolator.screenCoordinates.toTypedArray())
    }
}
