package com.wamteavm.models.screenobjects

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.files.Assets
import com.wamteavm.inputelements.InputElement
import com.wamteavm.inputelements.SelectBoxInput
import com.wamteavm.inputelements.TextInput
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.*
import com.wamteavm.screens.AnimationScreen
import com.wamteavm.utilities.AreaColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Unit(
    override var position: Coordinate,
    override val initTime: Int,
    var image: String = ""
) : ScreenObject(), HasAlpha, HasColor {
    override val posInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override val alpha = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()

    override var color: AreaColor = AreaColor.BLUE
    var name: String = ""
    var type: String = "infantry.png"
    var size: String = "XX"
    var drawSize: Float? = 1.0f
    @Transient private var typeTexture: Texture? = null
    @Transient var countryTexture: Texture? = null
    @Transient var width: Float = AnimationScreen.DEFAULT_UNIT_WIDTH.toFloat()
    @Transient var height: Float = AnimationScreen.DEFAULT_UNIT_HEIGHT.toFloat()

    override fun init() {
        super.init()
        buildInputs()
        countryTexture()
        typeTexture()
        alpha.updateInterpolationFunction()
    }

    override fun showInputs(verticalGroup: VerticalGroup, uiVisitor: UIVisitor) {
        uiVisitor.show(verticalGroup ,this)
    }

    override fun buildInputs() {
        super<ScreenObject>.buildInputs()
        super<HasAlpha>.buildInputs()
        super<HasColor>.buildInputs()

        inputElements.add(TextInput(null, { input ->
            size = input?: ""
        }, label@{
            return@label size
        }, String::class.java, "Set size"))
        inputElements.add(TextInput(null, { input ->
            drawSize = if (input != null && input != 0f) {
                input
            } else {
                null
            }
        }, label@{
            return@label drawSize.toString()
        }, Float::class.java, "Set draw size"))
        inputElements.add(SelectBoxInput(null, { input ->
            if (input != null) {
                type = input
                updateTypeTexture()
            }
        }, label@{
            println("inputting: $type")
            return@label type
        }, String::class.java, "Set type", Assets.unitTypes()))
        inputElements.add(SelectBoxInput(null, { input ->
            image = Assets.flagsPath(input ?: "")
            updateCountryTexture()
        }, label@{
            return@label image.substringAfter("assets/flags/")
        }, String::class.java, "Set country", Assets.countryNames))
        inputElements.add(TextInput(null, { input ->
            name = input ?: ""
        }, label@{
            return@label name
        }, String::class.java, "Set name"))
    }

    override fun clicked(x: Float, y: Float): Boolean {
        return ((x in (screenPosition.x - width * 0.5f)..(screenPosition.x + width * 0.5f)) && (y in (screenPosition.y - height * 0.5f)..(screenPosition.y + height * 0.5f)))
    }

    fun goToTime(time: Int, zoom: Float, cx: Float, cy: Float, paused: Boolean): Boolean {
        if (!paused) { alpha.evaluate(time) }
        return super.goToTime(time, zoom, cx, cy)
    }

    fun updateCountryTexture() {
        countryTexture = Assets.loadTexture(image)
    }

    fun countryTexture(): Texture? {
        if (countryTexture == null) {
            updateCountryTexture()
        }
        return countryTexture
    }

    fun updateTypeTexture() {
        typeTexture = Assets.loadTexture(Assets.unitKindsPath(type))
    }

    fun typeTexture(): Texture? {
        if (typeTexture == null) {
            updateTypeTexture()
        }
        return typeTexture
    }

    companion object {
        val sizePresets = mapOf(
            "XX" to 1.0f,
            "X" to 0.8f,
            "III" to 0.7f,
            "II" to 0.55f,
            "I" to 0.5f
        )
    }
}
