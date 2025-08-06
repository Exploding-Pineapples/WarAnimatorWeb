package com.wamteavm.models.screenobjects

import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.inputelements.InputElement
import com.wamteavm.inputelements.TextInput
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.*
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Arrow(override var position: Coordinate, override var initTime: Int): ScreenObject(), HasAlpha, HasColor {
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override val alpha: FloatSetPointInterpolator = FloatSetPointInterpolator()
    override var color = AreaColor.RED
    var thickness = 10f
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()

    override fun init() {
        super.init()
        alpha.updateInterpolationFunction()
    }

    override fun shouldDraw(time: Int): Boolean {
        return true
    }

    override fun showInputs(verticalGroup: VerticalGroup, uiVisitor: UIVisitor) {
        uiVisitor.show(verticalGroup, this)
    }

    fun goToTime(time: Int, zoom: Float, cx: Float, cy: Float, paused: Boolean): Boolean {
        if (!paused) { alpha.evaluate(time) }
        return super.goToTime(time, zoom, cx, cy)
    }

    override fun buildInputs() {
        super<ScreenObject>.buildInputs()
        super<HasAlpha>.buildInputs()
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
