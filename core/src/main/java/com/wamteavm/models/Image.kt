package com.wamteavm.models

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.inputelements.InputElement
import com.wamteavm.inputelements.SelectBoxInput
import com.wamteavm.inputelements.TextInput
import com.wamteavm.files.Assets
import com.wamteavm.interpolator.CoordinateSetPoints
import com.wamteavm.interpolator.FloatSetPoints
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Image(override var position: Coordinate, override var initTime: Int, var path: String) : ScreenObject(), HasAlpha {
    override val posSetPoints: CoordinateSetPoints = CoordinateSetPoints().apply { newSetPoint(initTime, position) }
    override var alpha = FloatSetPoints().apply { newSetPoint(initTime, 1f) }
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()
    var scale: Float = 1f

    @Transient var texture: Texture? = Assets.loadTexture(path)

    override fun showInputs(verticalGroup: VerticalGroup, uiVisitor: UIVisitor) {
        uiVisitor.show(verticalGroup, this)
    }

    fun loadTexture() {
        if (path == "") {
            println("Image path is empty")
        }
        texture = Assets.loadTexture(path)
        println("updated image path to $texture")
    }

    fun updateTexture(newPath: String) {
        path = newPath
        loadTexture()
    }

    fun goToTime(time: Int, zoom: Float, cx: Float, cy: Float, paused: Boolean): Boolean {
        if (!paused) { alpha.evaluate(time) }
        return super.goToTime(time, zoom, cx, cy)
    }

    override fun buildInputs() {
        super<ScreenObject>.buildInputs()
        super<HasAlpha>.buildInputs()

        inputElements.add(SelectBoxInput(null, { input ->
            updateTexture(Assets.mapsPath(input ?: ""))
        }, label@{
            return@label path.substringAfter("assets/maps/")
        }, String::class.java, "Image", Assets.images()))
        inputElements.add(
            TextInput(null, { input ->
                if (input != null) {
                    if (input >= 0) {
                        scale = input
                    }
                }
            }, label@{
                return@label scale.toString()
            }, Float::class.java, "Set scale")
        )
    }
}
