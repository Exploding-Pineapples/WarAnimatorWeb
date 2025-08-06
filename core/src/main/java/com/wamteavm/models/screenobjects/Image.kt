package com.wamteavm.models.screenobjects

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.inputelements.InputElement
import com.wamteavm.inputelements.SelectBoxInput
import com.wamteavm.inputelements.TextInput
import com.wamteavm.files.Assets
import com.wamteavm.interpolators.CoordinateSetPointInterpolator
import com.wamteavm.models.Coordinate
import com.wamteavm.models.Drawer
import com.wamteavm.models.ScreenObjectWithAlpha
import com.wamteavm.models.UIVisitor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Image(override var position: Coordinate, override var initTime: Int, var path: String = "") : ScreenObjectWithAlpha() {
    override val posSetPoints: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    @Transient override var inputElements: MutableList<InputElement<*>> = mutableListOf()
    var scale: Float = 1f

    @Transient var texture: Texture? = Assets.loadTexture(path)

    override fun init() {
        buildInputs()
        loadTexture()
    }

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }

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

    override fun buildInputs() {
        super.buildInputs()

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
