package com.wamteavm.models

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.wamteavm.ui.inputelements.InputElement
import com.wamteavm.WarAnimator.DISPLAY_HEIGHT
import com.wamteavm.WarAnimator.DISPLAY_WIDTH
import com.wamteavm.models.screenobjects.Arrow
import com.wamteavm.models.screenobjects.Image
import com.wamteavm.models.screenobjects.MapLabel
import com.wamteavm.models.screenobjects.Unit
import kotlinx.serialization.Serializable

@Serializable
data class Coordinate(
    var x: Float,
    var y: Float
)

interface HasInputs {
    var inputElements: MutableList<InputElement<*>>

    fun buildInputs() {
        inputElements = mutableListOf()
    }

    fun updateInputs() {
        if (inputElements == null) {
            inputElements = mutableListOf()
        }
    }
}

fun projectToScreen(position: Coordinate, zoom: Float, cx: Float, cy: Float): Coordinate {
    return Coordinate(
        position.x * zoom - cx * (zoom - 1) + (DISPLAY_WIDTH / 2 - cx),
        position.y * zoom - cy * (zoom - 1) + (DISPLAY_HEIGHT / 2 - cy)
    )
}

interface ID : Comparable<ID> {
    val value: Int

    override fun compareTo(other: ID): Int {
        return value - other.value
    }

    fun duplicate() : ID
}

@Serializable
class NodeCollectionID(override val value: Int = -1) : ID {
    override fun duplicate() : NodeCollectionID {
        return NodeCollectionID(value)
    }
}

@Serializable
class NodeID(override val value: Int = -1) : ID {
    override fun duplicate(): NodeID {
        return NodeID(value)
    }
}
