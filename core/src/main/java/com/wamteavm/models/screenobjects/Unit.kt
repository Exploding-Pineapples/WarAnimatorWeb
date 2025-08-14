package com.wamteavm.models.screenobjects

import com.wamteavm.interpolator.ColorSetPointInterpolator
import com.wamteavm.interpolator.CoordinateSetPointInterpolator
import com.wamteavm.interpolator.FloatSetPointInterpolator
import com.wamteavm.models.*
import com.wamteavm.screens.AnimationScreen
import com.wamteavm.utilities.ColorWrapper
import com.wamteavm.utilities.LoadedTexture
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Unit(
    override var position: Coordinate,
    override val initTime: Int,
) : ScreenObjectWithAlpha(), HasColor, Drawable {
    override val posInterpolator = CoordinateSetPointInterpolator().apply { newSetPoint(initTime, position) }
    override val alpha = FloatSetPointInterpolator().apply { newSetPoint(initTime, 1f) }
    override var color: ColorSetPointInterpolator = ColorSetPointInterpolator().apply { newSetPoint(initTime, ColorWrapper.parseString("red")!!) }
    override var order = "d"

    var country: LoadedTexture = LoadedTexture()
    var type: LoadedTexture = LoadedTexture()

    var size: String = "XX"
    var name: String = ""
    var drawSize: Float? = 1.0f

    @Transient var width: Float = AnimationScreen.DEFAULT_UNIT_WIDTH.toFloat()
    @Transient var height: Float = AnimationScreen.DEFAULT_UNIT_HEIGHT.toFloat()

    override fun init() {
        super<ScreenObjectWithAlpha>.init()
        super<HasColor>.init()
        type.loadTexture(null)
    }

    override fun clicked(x: Float, y: Float, zoom: Float): Boolean {
        val halfWidth = width * 0.5 / zoom
        val halfHeight = height * 0.5 / zoom
        return (x in (position.x - halfWidth)..(position.x + halfWidth)) && (y in (position.y - halfHeight)..(position.y + halfHeight))
    }

    override fun update(time: Int) {
        super<ScreenObjectWithAlpha>.update(time)
        super<HasColor>.update(time)
    }

    override fun draw(drawer: Drawer) {
        drawer.draw(this)
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
