package com.wamteavm.models.screenobjects

import com.badlogic.gdx.graphics.Texture
import com.wamteavm.loaders.InternalLoader
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Image(override var position: Coordinate, override var initTime: Int) : ScreenObjectWithAlpha(), Drawable {
    override var order = "a"
    var path: String = ""
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override var alpha = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    var scale: Float = 1f

    @Transient var texture: Texture? = InternalLoader.loadTexture(path)

    override fun init() {
        super.init()
        loadTexture()
    }

    override fun update(time: Int) {
        super.update(time)
    }

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }

    fun loadTexture() {
        if (path == "") {
            println("Image path is empty")
        }
        texture = InternalLoader.loadTexture(path)
        println("updated image path to $texture")
    }

    fun updateTexture(newPath: String) {
        path = newPath
        loadTexture()
    }
}
