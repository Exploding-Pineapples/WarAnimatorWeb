package com.wamteavm.models

import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.inputelements.InputElement
import com.wamteavm.inputelements.TextInput
import com.wamteavm.interpolator.CoordinateSetPoints
import com.wamteavm.interpolator.FloatSetPoints
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class MapLabel(override var position: Coordinate, override var initTime: Int) : ScreenObject(), HasAlpha, HasColor {
    override val posSetPoints: CoordinateSetPoints = CoordinateSetPoints().apply { newSetPoint(initTime, position) }
    override val alpha: FloatSetPoints = FloatSetPoints().apply { newSetPoint(initTime, 1f) }
    var text = ""
    override var color = AreaColor.RED
    var size = 1f
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()

    override fun showInputs(verticalGroup: VerticalGroup, uiVisitor: UIVisitor) {
        uiVisitor.show(verticalGroup, this)
    }

    fun goToTime(time: Int, zoom: Float, cx: Float, cy: Float, paused: Boolean): Boolean {
        if (!paused) { alpha.evaluate(time) }
        return super.goToTime(time, zoom, cx, cy)
    }

    override fun buildInputs() {
        super<ScreenObject>.buildInputs()
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
