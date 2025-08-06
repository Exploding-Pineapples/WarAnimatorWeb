package com.wamteavm.models

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Array
import com.wamteavm.ui.inputelements.InputElement
import com.wamteavm.ui.inputelements.SelectBoxInput
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.interpolator.NodeCollectionInterpolator
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
open class NodeCollection(override val id: NodeCollectionID) : AnyObject, HasInputs, HasID, HasAlpha, HasColor,
    Clickable {
    override var alpha: FloatSetPointInterpolator = FloatSetPointInterpolator()
    @Transient var interpolator: NodeCollectionInterpolator = NodeCollectionInterpolator()
    override var color: AreaColor = AreaColor.RED
    var type: String = "None"
    var width: Float? = null
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()

    override fun buildInputs() {
        super<HasInputs>.buildInputs()
        super<HasAlpha>.buildInputs()
        super<HasColor>.buildInputs()
    }

    override fun init() {
        alpha.updateInterpolationFunction()
        interpolator = NodeCollectionInterpolator()
        buildInputs()
    }

    fun update(time: Int, camera: OrthographicCamera, paused: Boolean) {
        if (!paused) {
            alpha.evaluate(time)
            //interpolator.updateInterpolationFunction()
        }
        interpolator.evaluate(time)
        interpolator.updateScreenCoordinates(camera)
    }

    fun draw(drawer: Drawer) {
        drawer.draw(this)
    }

    override fun clicked(x: Float, y: Float): Boolean {
        return clickedCoordinates(x, y, interpolator.screenCoordinates.toTypedArray())
    }
}
