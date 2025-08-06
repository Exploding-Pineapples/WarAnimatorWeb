package com.wamteavm.models

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Array
import com.wamteavm.inputelements.InputElement
import com.wamteavm.inputelements.SelectBoxInput
import com.wamteavm.interpolators.ColorSetPointInterpolator
import com.wamteavm.interpolators.FloatSetPointInterpolator
import com.wamteavm.interpolators.NodeCollectionInterpolator
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
open class NodeCollection(override val id: NodeCollectionID, val initTime: Int) : AnyObject, HasInputs, HasID, HasAlpha, HasColor, Clickable {
    override var alpha: FloatSetPointInterpolator = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    override var color: ColorSetPointInterpolator = ColorSetPointInterpolator().apply { newSetPoint(initTime, AreaColor.BLUE) }
    @Transient var interpolator: NodeCollectionInterpolator = NodeCollectionInterpolator()
    var type: String = "None"
    var width: Float? = null
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()

    override fun buildInputs() {
        super<HasInputs>.buildInputs()
        super<HasAlpha>.buildInputs()
        super<HasColor>.buildInputs()

        inputElements.add(
            SelectBoxInput(null, { input ->
                type = input?: "None"
                if (type == "Line") {
                    width = 5f
                }
            }, label@{
                return@label type
            }, String::class.java, "Set node collection type", Array<String>().apply { add("Area", "Line") })
        )
    }

    override fun init() {
        alpha.updateInterpolationFunction()
        color.updateInterpolationFunction()
        interpolator = NodeCollectionInterpolator()
        buildInputs()
    }

    override fun showInputs(verticalGroup: VerticalGroup, uiVisitor: UIVisitor) {
        uiVisitor.show(verticalGroup, this)
    }

    override fun hideInputs(verticalGroup: VerticalGroup, uiVisitor: UIVisitor) {
        super<HasInputs>.hideInputs(verticalGroup, uiVisitor)
        super<HasAlpha>.hideInputs(verticalGroup, uiVisitor)
    }

    fun update(time: Int, camera: OrthographicCamera, paused: Boolean) {
        if (!paused) {
            alpha.evaluate(time)
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
