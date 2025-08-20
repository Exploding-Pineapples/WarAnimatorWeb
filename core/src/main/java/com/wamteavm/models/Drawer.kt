package com.wamteavm.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Rectangle
import com.wamteavm.WarAnimator.DISPLAY_HEIGHT
import com.wamteavm.WarAnimator.DISPLAY_WIDTH
import com.wamteavm.models.screenobjects.Arrow
import com.wamteavm.models.screenobjects.Image
import com.wamteavm.models.screenobjects.Label
import com.wamteavm.models.screenobjects.Unit
import com.wamteavm.models.screenobjects.Unit.Companion.sizePresets
import com.wamteavm.screens.AnimationScreen
import com.wamteavm.utilities.Earcut
import com.wamteavm.utilities.colorWithAlpha
import com.wamteavm.utilities.measureText
import com.wamteavm.utilities.projectToScreen
import space.earlygrey.shapedrawer.JoinType
import space.earlygrey.shapedrawer.ShapeDrawer
import kotlin.math.min
import kotlin.math.sqrt

class Drawer(val font: BitmapFont,
             private val fontShader: ShaderProgram,
             private val batcher: SpriteBatch,
             private val shapeDrawer: ShapeDrawer,
             private var camera: OrthographicCamera,
             var time: Int = 0
) {
    private var zoomFactor: Float = 1f
    private var animationMode = false
    private val drawOrder = sortedMapOf<String, MutableList<Drawable>>()

    fun init(animation: Animation) {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        updateDrawOrder(animation)
    }

    fun updateDrawOrder(animation: Animation) {
        drawOrder.clear()
        animation.images.forEach { addToDrawOrder(it) }
        animation.arrows.forEach { addToDrawOrder(it) }
        animation.units.forEach { addToDrawOrder(it) }
        animation.labels.forEach { addToDrawOrder(it) }
        animation.nodeCollections.forEach { addToDrawOrder(it) }
    }

    fun addToDrawOrder(it: Drawable) {
        if (drawOrder.containsKey(it.order)) {
            drawOrder[it.order]!!.add(it)
        } else {
            drawOrder[it.order] = mutableListOf(it)
        }
    }

    fun update(time: Int, animationMode: Boolean) {
        this.time = time
        this.animationMode = animationMode
        zoomFactor = 1f
    }

    fun draw(animation: Animation) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batcher.setColor(1f, 1f, 1f, 1f) // Reset to full transparency

        for (frame in drawOrder) {
            frame.value.forEach {
                it.draw(this)
            }
        }
        if (animationMode) {
            animation.nodes.forEach { draw(it) }
        }
    }

    fun draw(nodeCollection: NodeCollection) {
        for (setPoint in nodeCollection.interpolator.value) {
            val screenCoordinates = FloatArray(setPoint.size)
            for (i in setPoint.indices step 2) { // project like this instead of using projectToScreen() to avoid boxing of Coordinate class
                screenCoordinates[i] =
                    setPoint[i] * camera.zoom - camera.position.x * (camera.zoom - 1) + (DISPLAY_WIDTH / 2 - camera.position.x)
                screenCoordinates[i + 1] =
                    setPoint[i + 1] * camera.zoom - camera.position.y * (camera.zoom - 1) + (DISPLAY_HEIGHT / 2 - camera.position.y)
            }

            if (screenCoordinates.isNotEmpty()) {
                shapeDrawer.setColor(colorWithAlpha(nodeCollection.color.value.color, nodeCollection.alpha.value))
                if (nodeCollection.type == "Area") {
                    val earcut = Earcut.earcut(screenCoordinates) // Look up polygon earclip

                    var j = 0
                    while (j < earcut.size) {
                        shapeDrawer.filledTriangle(
                            screenCoordinates[earcut[j] * 2],
                            screenCoordinates[earcut[j] * 2 + 1],
                            screenCoordinates[earcut[j + 1] * 2],
                            screenCoordinates[earcut[j + 1] * 2 + 1],
                            screenCoordinates[earcut[j + 2] * 2],
                            screenCoordinates[earcut[j + 2] * 2 + 1]
                        )
                        j += 3
                    }
                }
                if (nodeCollection.type == "Line") {
                    shapeDrawer.path(screenCoordinates, nodeCollection.width ?: 5f, JoinType.NONE, true)
                }
            }
        }
    }

    fun draw(unit: Unit) {
        val drawSize = (unit.drawSize ?: sizePresets[unit.size]) ?: 1.0f
        unit.width = AnimationScreen.DEFAULT_UNIT_WIDTH * zoomFactor * drawSize
        unit.height = AnimationScreen.DEFAULT_UNIT_HEIGHT * zoomFactor * drawSize
        val screenPosition = projectToScreen(unit.position, camera.zoom, camera.position.x, camera.position.y)

        if (unit.alpha.value == 0f) {
            if (animationMode) {
                shapeDrawer.setColor(colorWithAlpha(Color.BLACK, 0.5f))
                shapeDrawer.filledRectangle(screenPosition.x - unit.width / 2, screenPosition.y - unit.width / 2, unit.width, unit.height)
            }
        } else {
            val padding = unit.width / 16

            shapeDrawer.setColor(colorWithAlpha(unit.color.value.color, unit.alpha.value)) // Outline
            shapeDrawer.filledRectangle(centerRect(screenPosition.x, screenPosition.y, unit.width, unit.height))

            shapeDrawer.setColor(colorWithAlpha(Color.LIGHT_GRAY, unit.alpha.value)) // Center light gray contrast area
            shapeDrawer.filledRectangle(centerRect(screenPosition.x, screenPosition.y, unit.width - 2 * padding, unit.height - 2 * padding))

            if (unit.alpha.value != 0f) {
                batcher.setColor(1f, 1f, 1f, unit.alpha.value)
                if (unit.type.loaded) {
                    drawTexture(
                        unit.type.getTexture()!!,
                        centerRect(screenPosition.x, screenPosition.y, unit.width / 1.5f, unit.height / 1.5f)
                    )
                }
                if (unit.country.loaded) {
                    batcher.draw(
                        unit.country.getTexture()!!,
                        screenPosition.x - unit.width / 2f + padding,
                        screenPosition.y + unit.height / 2f - unit.height / 4f - padding,
                        unit.width / 4.0f, unit.height / 4.0f
                    )
                }
            }

            prepareFont(Color.WHITE, unit.color.value.color, unit.alpha.value, 0.5f * zoomFactor * drawSize)

            val sizeSize = measureText(font, unit.size)
            font.draw(
                batcher,
                unit.size,
                screenPosition.x + unit.width / 2 - sizeSize.width - padding - sizeSize.height * 0.1f,
                screenPosition.y + unit.height / 2 - padding
            )

            if (unit.name != "") {
                val nameSize = measureText(font, unit.name)
                font.draw(
                    batcher,
                    unit.name,
                    screenPosition.x - nameSize.width / 2,
                    screenPosition.y - unit.height / 2 + nameSize.height + padding
                )
            }

            batcher.shader = null
        }
    }

    fun draw(node: Node) {
        val screenPosition = projectToScreen(node.position, camera.zoom, camera.position.x, camera.position.y)
        if (node.timeDefined(time)) {
            shapeDrawer.setColor(Color.GREEN)
            shapeDrawer.filledCircle(screenPosition.x, screenPosition.y, 7.0f)
        }
    }

    fun draw(arrow: Arrow) {
        var previous = projectToScreen(arrow.posInterpolator.evaluate(arrow.posInterpolator.setPoints.keys.first()), camera.zoom, camera.position.x, camera.position.y)

        shapeDrawer.setColor(colorWithAlpha(arrow.color.value.color, arrow.alpha.value))

        val endTime = min(time, arrow.posInterpolator.setPoints.keys.last())

        for (time in arrow.posInterpolator.setPoints.keys.first().toInt()..endTime) { // Draws entire body of arrow
            val position = projectToScreen(arrow.posInterpolator.evaluate(time), camera.zoom, camera.position.x, camera.position.y
            )
            shapeDrawer.line(previous.x, previous.y, position.x, position.y, arrow.thickness)
            if (time == endTime) {
                val triangle = generateTriangle(previous, position, arrow.thickness * 2, arrow.thickness * 3)
                shapeDrawer.filledTriangle(
                    triangle[0].x,
                    triangle[0].y,
                    triangle[1].x,
                    triangle[1].y,
                    triangle[2].x,
                    triangle[2].y
                )
            }
            previous = position
        }
    }

    fun draw(image: Image) {
        val screenPosition = projectToScreen(image.position, camera.zoom, camera.position.x, camera.position.y)
        if (animationMode) {
            drawAsSelected(image)
        }
        if (image.texture.loaded && image.alpha.value != 0f) {
            batcher.color = colorWithAlpha(Color.WHITE, image.alpha.value)
            batcher.draw(
                image.texture.getTexture(),
                screenPosition.x,
                screenPosition.y,
                image.texture.getTexture()!!.width.toFloat() * camera.zoom * image.scale,
                image.texture.getTexture()!!.height.toFloat() * camera.zoom * image.scale
            )
        }
    }

    fun draw(label: Label) {
        val screenPosition = projectToScreen(label.position, camera.zoom, camera.position.x, camera.position.y)

        shapeDrawer.setColor(Color(label.color.value.red, label.color.value.green, label.color.value.blue, label.alpha.value))
        shapeDrawer.filledCircle(screenPosition.x, screenPosition.y, label.size * 10)

        batcher.setColor(1f, 1f, 1f, label.alpha.value)

        prepareFont(Color.WHITE, label.color.value.color, label.alpha.value, label.size)

        val textSize = measureText(font, label.text)
        font.draw(batcher, label.text, screenPosition.x - textSize.width / 2, screenPosition.y + textSize.height * (3f / 2) + label.size * 5)

        batcher.shader = null
    }

    private fun drawTexture(texture: Texture, rect: Rectangle) {
        batcher.draw(texture, rect.x, rect.y, rect.width, rect.height)
    }

    fun drawAsSelected(anyObject: AnyObject) {
        if (HasInterpolatedPosition::class.java.isAssignableFrom(anyObject.javaClass)) {
            val hasPosition = anyObject as HasInterpolatedPosition
            val posInterpolator = hasPosition.posInterpolator

            if (posInterpolator.interpolated && posInterpolator.setPoints.size > 1) {
                shapeDrawer.setColor(Color.SKY)
                for (time in posInterpolator.setPoints.keys.first().toInt()..posInterpolator.setPoints.keys.last()
                    .toInt() step 4) { // Draws entire path of the selected object over time
                    val position = projectToScreen(
                        posInterpolator.evaluate(time),
                        camera.zoom,
                        camera.position.x,
                        camera.position.y
                    )
                    shapeDrawer.filledCircle(position.x, position.y, 2f)
                }
                shapeDrawer.setColor(Color.PURPLE)
                for (time in posInterpolator.setPoints.keys) { // Draws all set points of the selected object
                    val position = projectToScreen(
                        posInterpolator.evaluate(time),
                        camera.zoom,
                        camera.position.x,
                        camera.position.y
                    )
                    shapeDrawer.filledCircle(position.x, position.y, 4f)
                }
            }

            val screenPosition = projectToScreen(hasPosition.position, camera.zoom, camera.position.x, camera.position.y)

            if (anyObject.javaClass == Unit::class.java) {
                val unit = anyObject as Unit

                shapeDrawer.setColor(colorWithAlpha(Color.BLACK, 0.5f))
                shapeDrawer.filledRectangle(centerRect(screenPosition.x, screenPosition.y, unit.width, unit.height))
            }

            if (anyObject.javaClass == Image::class.java) {
                shapeDrawer.setColor(Color.ORANGE)
                shapeDrawer.filledRectangle(centerRect(screenPosition.x, screenPosition.y, 12f, 12f))
            }
        }

        if (anyObject.javaClass.isAssignableFrom(Edge::class.java)) {
            val screenCoords = (anyObject as Edge).coords.map { projectToScreen(it, camera.zoom, camera.position.x, camera.position.y) }
            for (i in 0..<screenCoords.size - 1) {
                shapeDrawer.line(screenCoords[i].x, screenCoords[i].y, screenCoords[i + 1].x, screenCoords[i + 1].y, 5f)
            }
        }

        if (anyObject.javaClass.isAssignableFrom(Node::class.java)) {
            val screenCoords = projectToScreen((anyObject as Node).position, camera.zoom, camera.position.x, camera.position.y)
            shapeDrawer.filledCircle(screenCoords.x, screenCoords.y, 7.0f)
        }
    }

    private fun prepareFont(color: Color, outlineColor: Color, alpha: Float, size: Float) {
        font.color = colorWithAlpha(color, alpha)
        font.data.setScale(size)
        batcher.shader = fontShader
        fontShader.setUniformf("scale", 1.0f)
        fontShader.setUniformf("outlineDistance", 0.05f)
        fontShader.setUniformf("outlineColor", colorWithAlpha(outlineColor, alpha))
    }

    private fun generateTriangle(a: Coordinate, b: Coordinate, baseWidth: Float, height: Float): Array<Coordinate> {
        // Direction from A to B
        val dx: Float = b.x - a.x
        val dy: Float = b.y - a.y
        val length = sqrt(dx * dx + dy * dy)
        val ux = dx / length
        val uy = dy / length

        // Perpendicular direction (rotated 90°)
        val px = -uy

        // Base endpoints, perpendicular to direction, centered at B
        val halfBase = baseWidth / 2.0f
        val p1 = Coordinate(b.x + px * halfBase, b.y + ux * halfBase)
        val p2 = Coordinate(b.x - px * halfBase, b.y - ux * halfBase)

        // Tip of triangle, extending in A→B direction from B
        val tip = Coordinate(b.x + ux * height, b.y + uy * height)

        return arrayOf(p1, p2, tip)
    }
}

fun centerRect(x: Float, y: Float, width: Float, height: Float): Rectangle {
    return Rectangle(x - width / 2, y - height / 2, width, height)
}
