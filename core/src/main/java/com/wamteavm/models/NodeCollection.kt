package com.wamteavm.models

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Array
import com.wamteavm.inputelements.InputElement
import com.wamteavm.inputelements.SelectBoxInput
import com.wamteavm.interpolator.LinearInterpolatedFloat
import com.wamteavm.interpolator.NodeCollectionInterpolator
import com.wamteavm.models.*
import com.wamteavm.utilities.AreaColor

open class NodeCollection(override val id: NodeCollectionID) : AnyObject, HasInputs, HasID, HasAlpha, HasColor,
    Clickable {
    override var alpha = LinearInterpolatedFloat(1f, 0)
    @Transient var interpolator: NodeCollectionInterpolator = NodeCollectionInterpolator()
    override var color: AreaColor = AreaColor.RED
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

    fun init(initTime: Int) {
        alpha.update(initTime)
        if (interpolator == null) {
            interpolator = NodeCollectionInterpolator()
        }
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
            alpha.update(time)
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
