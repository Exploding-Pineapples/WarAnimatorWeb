package com.wamteavm.models.screenobjects

import com.badlogic.gdx.graphics.Texture
import com.wamteavm.loaders.InternalLoader
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Image(override var position: Coordinate, override var initTime: Int) : ScreenObject(),
    HasAlpha, Drawable {
    override var order = "a"
    var path: String = ""
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override var alpha = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    var scale: Float = 1f

    @Transient var texture: Texture? = InternalLoader.loadTexture(path)

    override fun init() {
        super.init()
        loadTexture()
        alpha.updateInterpolationFunction()
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

    fun goToTime(time: Int, zoom: Float, cx: Float, cy: Float, paused: Boolean) {
        if (!paused) { alpha.evaluate(time) }
        super.goToTime(time, zoom, cx, cy)
    }
}
