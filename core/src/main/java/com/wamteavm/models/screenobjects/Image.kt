package com.wamteavm.models.screenobjects

import com.badlogic.gdx.graphics.Texture
import com.wamteavm.loaders.InternalLoader
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.loaders.externalloaders.AbstractExternalLoader
import com.wamteavm.models.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Image(override var position: Coordinate, override var initTime: Int) : ScreenObjectWithAlpha(), Drawable {
    override val posInterpolator: CoordinateSetPointInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override var alpha = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    override var order = "a"

    var key: String = ""
    var scale: Float = 1f

    @Transient var texture: Texture? = InternalLoader.loadTexture(key)

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
    }

    fun loadTexture(loader: AbstractExternalLoader) {
        if (key == "") {
            println("Empty image key")
        }
        texture = loader.loadedImages[key]
        println("updated image key to $key")
    }
}
