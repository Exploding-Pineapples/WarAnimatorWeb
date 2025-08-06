package com.wamteavm.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Rectangle
import com.wamteavm.models.Unit.Companion.sizePresets
import com.wamteavm.utilities.Earcut
import com.wamteavm.screens.AnimationScreen
import com.wamteavm.utilities.measureText
import space.earlygrey.shapedrawer.JoinType
import space.earlygrey.shapedrawer.ShapeDrawer
import kotlin.math.min
import kotlin.math.sqrt

class Drawer(val font: BitmapFont,
             val fontShader: ShaderProgram,
             val batcher: SpriteBatch,
             val shapeDrawer: ShapeDrawer,
             var camera: OrthographicCamera,
             var time: Int = 0
) {
    private var zoomFactor: Float = 1f
    var animationMode = false

    fun update(time: Int, animationMode: Boolean) {
        this.time = time
        this.animationMode = animationMode
        zoomFactor = 1f
    }

    fun draw(animation: Animation) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batcher.setColor(1f, 1f, 1f, 1f) // Reset to full transparency

        animation.images.forEach { draw(it) }
        animation.mapLabels.forEach { draw(it) }

        for (nodeCollection in animation.nodeCollections) {
            if (nodeCollection.type == "Area") {
                draw(nodeCollection)
            }
        }

        //batcher.setColor(1f, 1f, 1f, 1f)
        animation.units.forEach { draw(it) }

        for (edgeCollection in animation.nodeCollections) {
            if (edgeCollection.type == "Line") {
                draw(edgeCollection)
            }
        }

        if (animationMode) animation.nodes.forEach { draw(it) }
        animation.arrows.forEach { draw(it) }
    }

    fun draw(nodeCollection: NodeCollection) {
        val coords = nodeCollection.interpolator.screenCoordinates

        if (coords.isNotEmpty()) {
            shapeDrawer.setColor(colorWithAlpha(nodeCollection.color.color, nodeCollection.alpha.value))
            if (nodeCollection.type == "Area") {
                val earcut = Earcut.earcut(coords) // Turns polygon into series of triangles which share vertices with the polygon. The triangles' vertices are represented as the index of an original polygon vertex

                var j = 0
                while (j < earcut.size) {
                    shapeDrawer.filledTriangle(
                        coords[earcut[j] * 2],
                        coords[earcut[j] * 2 + 1],
                        coords[earcut[j + 1] * 2],
                        coords[earcut[j + 1] * 2 + 1],
                        coords[earcut[j + 2] * 2],
                        coords[earcut[j + 2] * 2 + 1]
                    )
                    j += 3
                }
            }
            if (nodeCollection.type == "Line") {
                shapeDrawer.path(coords, nodeCollection.width?: 5f, JoinType.NONE, true)
            }
        }
    }

    fun draw(unit: Unit) {
        val drawSize = (unit.drawSize ?: sizePresets[unit.size]) ?: 1.0f
        unit.width = AnimationScreen.DEFAULT_UNIT_WIDTH * zoomFactor * drawSize
        unit.height = AnimationScreen.DEFAULT_UNIT_HEIGHT * zoomFactor * drawSize

        if (unit.alpha.value == 0f) {
            if (animationMode) {
                shapeDrawer.setColor(colorWithAlpha(Color.BLACK, 0.5f))
                shapeDrawer.filledRectangle(unit.screenPosition.x - unit.width / 2, unit.screenPosition.y - unit.width / 2, unit.width, unit.height)
            }
        } else {
            val padding = unit.width / 16

            shapeDrawer.setColor(colorWithAlpha(unit.color.color, unit.alpha.value))
            shapeDrawer.filledRectangle(
                unit.screenPosition.x - unit.width * 0.5f,
                unit.screenPosition.y - unit.height * 0.5f,
                unit.width,
                unit.height
            )

            shapeDrawer.setColor(colorWithAlpha(Color.LIGHT_GRAY, unit.alpha.value))
            shapeDrawer.filledRectangle(
                unit.screenPosition.x - unit.width * 0.5f + padding,
                unit.screenPosition.y - unit.height * 0.5f + padding,
                unit.width - 2 * padding,
                unit.height - 2 * padding
            )

            batcher.setColor(1f, 1f, 1f, unit.alpha.value)
            if (unit.typeTexture() != null) {
                batcher.draw(
                    unit.typeTexture(),
                    unit.screenPosition.x - unit.width / 3f,
                    unit.screenPosition.y - unit.height / 3f,
                    unit.width / 1.5f,
                    unit.height / 1.5f
                )
            }
            if (unit.countryTexture() != null) {
                batcher.draw(
                    unit.countryTexture,
                    unit.screenPosition.x - unit.width / 2f + padding,
                    unit.screenPosition.y + unit.height / 2f - unit.height / 4f - padding,
                    unit.width / 4.0f, unit.height / 4.0f
                )
            }

            prepareFont(Color.WHITE, unit.color.color, unit.alpha.value, 0.5f * zoomFactor * drawSize)

            val sizeSize = measureText(font, unit.size)
            font.draw(
                batcher,
                unit.size,
                unit.screenPosition.x + unit.width / 2 - sizeSize.width - padding - sizeSize.height * 0.1f,
                unit.screenPosition.y + unit.height / 2 - padding
            )

            if (unit.name != "") {
                val nameSize = measureText(font, unit.name)
                font.draw(
                    batcher,
                    unit.name,
                    unit.screenPosition.x - nameSize.width / 2,
                    unit.screenPosition.y - unit.height / 2 + nameSize.height + padding
                )
            }

            batcher.shader = null
        }
    }

    fun draw(node: Node) {
        if (time == node.initTime) {
            shapeDrawer.setColor(Color.GREEN)
            shapeDrawer.filledCircle(node.screenPosition.x, node.screenPosition.y, 7.0f)
        }
    }

    fun draw(arrow: Arrow) {
        var previous = projectToScreen(arrow.posSetPoints.evaluate(arrow.posSetPoints.setPoints.keys.first()), camera.zoom, camera.position.x, camera.position.y)

        shapeDrawer.setColor(colorWithAlpha(arrow.color.color, arrow.alpha.value))

        val endTime = min(time, arrow.posSetPoints.setPoints.keys.last())

        for (time in arrow.posSetPoints.setPoints.keys.first().toInt()..endTime) { // Draws entire body of arrow
            val position = projectToScreen(arrow.posSetPoints.evaluate(time), camera.zoom, camera.position.x, camera.position.y
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
        if (animationMode) {
            drawAsSelected(image)
        }
        if (image.texture != null && image.alpha.value != 0f) {
            batcher.color = colorWithAlpha(Color.WHITE, image.alpha.value)
            batcher.draw(
                image.texture,
                image.screenPosition.x,
                image.screenPosition.y,
                image.texture!!.width.toFloat() * camera.zoom * image.scale,
                image.texture!!.height.toFloat() * camera.zoom * image.scale
            )
        }
    }

    fun draw(mapLabel: MapLabel) {
        shapeDrawer.setColor(Color(mapLabel.color.color.r, mapLabel.color.color.g, mapLabel.color.color.b, mapLabel.alpha.value))
        shapeDrawer.filledCircle(mapLabel.screenPosition.x, mapLabel.screenPosition.y, mapLabel.size * 10)

        batcher.setColor(1f, 1f, 1f, mapLabel.alpha.value)

        prepareFont(Color.WHITE, mapLabel.color.color, mapLabel.alpha.value, mapLabel.size)

        val textSize = measureText(font, mapLabel.text)
        font.draw(batcher, mapLabel.text, mapLabel.screenPosition.x - textSize.width / 2, mapLabel.screenPosition.y + textSize.height * (3f / 2) + mapLabel.size * 5)

        batcher.shader = null
    }

    fun drawTexture(texture: Texture, rect: Rectangle) {
        batcher.draw(texture, rect.x, rect.y, rect.width, rect.height)
    }

    fun drawAsSelected(anyObject: AnyObject) {
        if (InterpolatedObject::class.java.isAssignableFrom(anyObject.javaClass)) {
            val screenObject = anyObject as InterpolatedObject
            val posInterpolator = screenObject.posSetPoints

            shapeDrawer.setColor(Color.SKY)
            for (time in posInterpolator.setPoints.keys.first().toInt()..posInterpolator.setPoints.keys.last()
                .toInt() step 4) { // Draws entire path of the selected object over time
                val position = projectToScreen(posInterpolator.evaluate(time), camera.zoom, camera.position.x, camera.position.y)
                shapeDrawer.filledCircle(position.x, position.y, 2f)
            }
            shapeDrawer.setColor(Color.PURPLE)
            for (time in posInterpolator.setPoints.keys) { // Draws all set points of the selected object
                val position = projectToScreen(posInterpolator.evaluate(time), camera.zoom, camera.position.x, camera.position.y)
                shapeDrawer.filledCircle(position.x, position.y, 4f)
            }

            if (anyObject.javaClass == Unit::class.java) {
                val unit = anyObject as Unit
                shapeDrawer.setColor(colorWithAlpha(Color.BLACK, 0.5f))
                shapeDrawer.line(
                    unit.screenPosition.x - unit.width / 2,
                    unit.screenPosition.y - unit.width / 2,
                    unit.width,
                    unit.height
                )
            }
            if (anyObject.javaClass.isAssignableFrom(HasScreenPosition::class.java)) {
                shapeDrawer.setColor(Color.ORANGE)
                shapeDrawer.filledRectangle(
                    (anyObject as HasScreenPosition).screenPosition.x - 6.0f,
                    anyObject.screenPosition.y - 6.0f,
                    12f,
                    12f
                ) // Draws an orange square to symbolize being selected
            }
        }
        if (anyObject.javaClass.isAssignableFrom(Edge::class.java)) {
            val screenCoords = (anyObject as Edge).screenCoords
            for (i in 0..<screenCoords.size - 1) {
                shapeDrawer.line(screenCoords[i].x, screenCoords[i].y, screenCoords[i + 1].x, screenCoords[i + 1].y, 5f)
            }
        }
        if (anyObject.javaClass.isAssignableFrom(Node::class.java)) {
            val screenCoords = (anyObject as Node).screenPosition
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

fun colorWithAlpha(color: Color, alpha: Float): Color {
    return Color(color.r, color.g, color.b, alpha)
}

fun centerRect(x: Float, y: Float, width: Float, height: Float): Rectangle {
    return Rectangle(x - width / 2, y - height / 2, width, height)
}
