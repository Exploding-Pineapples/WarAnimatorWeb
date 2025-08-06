package com.wamteavm.models.screenobjects

import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.inputelements.TextInput
import com.wamteavm.interpolators.ColorSetPointInterpolator
import com.wamteavm.interpolators.CoordinateSetPointInterpolator
import com.wamteavm.models.*
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable

@Serializable
class Arrow(override var position: Coordinate, override var initTime: Int): ScreenObjectWithAlpha(), HasColor {
    override val posSetPoints: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override var color: ColorSetPointInterpolator = ColorSetPointInterpolator().apply { newSetPoint(initTime, AreaColor.BLACK) }
    var thickness = 10f

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

        inputElements.add(
            TextInput(null, { input ->
                if (input != null) {
                    thickness = input
                }
            }, label@{
                return@label thickness.toString()
            }, Float::class.java, "Set thickness")
        )
    }
}
