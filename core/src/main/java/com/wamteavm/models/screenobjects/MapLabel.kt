package com.wamteavm.models.screenobjects

import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.inputelements.InputElement
import com.wamteavm.inputelements.TextInput
import com.wamteavm.interpolators.ColorSetPointInterpolator
import com.wamteavm.interpolators.CoordinateSetPointInterpolator
import com.wamteavm.models.*
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class MapLabel(override var position: Coordinate, override var initTime: Int) : ScreenObjectWithAlpha(), HasAlpha, HasColor {
    override val posSetPoints: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override var color: ColorSetPointInterpolator = ColorSetPointInterpolator().apply { newSetPoint(initTime, AreaColor.RED) }
    var text = ""
    var size = 1f
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()

    override fun init() {
        buildInputs()
        color.updateInterpolationFunction()
    }

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }

    override fun showInputs(verticalGroup: VerticalGroup, uiVisitor: UIVisitor) {
        uiVisitor.show(verticalGroup, this)
    }

    override fun buildInputs() {
        super<ScreenObjectWithAlpha>.buildInputs()
        super<HasColor>.buildInputs()
        super<HasAlpha>.buildInputs()

        inputElements.add(
            TextInput(null, { input ->
                if (input != null) {
                    if (input > 0) {
                        size = input
                    }
                }
            }, label@{
                return@label size.toString()
            }, Float::class.java, "Set size")
        )
        inputElements.add(TextInput(null, { input ->
            text = input ?: ""
        }, label@{
            return@label text
        }, String::class.java, "Set text"))
    }
}
