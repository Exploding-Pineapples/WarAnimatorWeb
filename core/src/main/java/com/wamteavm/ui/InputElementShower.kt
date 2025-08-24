package com.wamteavm.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Array
import com.wamteavm.loaders.InternalLoader
import com.wamteavm.loaders.externalloaders.AbstractExternalLoader
import com.wamteavm.models.*
import com.wamteavm.models.screenobjects.Arrow
import com.wamteavm.models.screenobjects.Image
import com.wamteavm.models.screenobjects.Label
import com.wamteavm.models.screenobjects.Unit
import com.wamteavm.ui.inputelements.CheckBoxInput
import com.wamteavm.ui.inputelements.InputElement
import com.wamteavm.ui.inputelements.SelectBoxInput
import com.wamteavm.ui.inputelements.TextInput
import com.wamteavm.utilities.ColorWrapper
import com.wamteavm.utilities.gdxArrayOf

class InputElementShower(val skin: Skin, val animation: Animation, private val externalLoader: AbstractExternalLoader) {
    private val inputElements: MutableList<InputElement<*>> = mutableListOf()

    fun hideAll(verticalGroup: VerticalGroup) {
        inputElements.forEach { it.hide(verticalGroup) }
    }

    fun showAll(verticalGroup: VerticalGroup) {
        inputElements.forEach { it.show(verticalGroup, skin) }
    }

    fun update(verticalGroup: VerticalGroup, objects: List<AnyObject>, time: Int) {
        inputElements.forEach { it.hide(verticalGroup) }
        inputElements.clear()

        val hasAlphas: MutableList<HasAlpha> = mutableListOf()
        val hasColors: MutableList<HasColor> = mutableListOf()
        val drawables: MutableList<Drawable> = mutableListOf()
        val classElementMap: MutableMap<Class<HasInputs>, MutableList<HasInputs>> = mutableMapOf()

        for (element in objects) {
            if (HasInputs::class.java.isAssignableFrom(element.javaClass)) {
                if (HasAlpha::class.java.isAssignableFrom(element.javaClass)) {
                    hasAlphas.add(element as HasAlpha)
                }
                if (HasColor::class.java.isAssignableFrom(element.javaClass)) {
                    hasColors.add(element as HasColor)
                }
                if (Drawable::class.java.isAssignableFrom(element.javaClass)) {
                    drawables.add(element as Drawable)
                }
                if (classElementMap.containsKey((element as HasInputs).javaClass)) {
                    classElementMap[element.javaClass]!!.add(element)
                } else {
                    classElementMap[element.javaClass] = mutableListOf(element)
                }
            }
        }

        if (drawables.isNotEmpty()) {
            val layerInput = TextInput(null, { input ->
                for (drawable in drawables) {
                    drawable.order = input ?: ""
                }
                animation.drawer.updateDrawOrder(animation)
            }, label@{
                return@label returnPropertyIfSame(drawables) { it.order }
            }, String::class.java, "Set layer/draw order")
            inputElements.add(layerInput)
        }

        if (hasAlphas.isNotEmpty()) {
            inputElements.add(TextInput(null, { input ->
                if (input != null) {
                    for (element in hasAlphas) {
                        element.alpha.newSetPoint(time, input)
                        element.alpha.value = input
                    }
                } else {
                    for (element in hasAlphas) {
                        element.alpha.removeFrame(time)
                    }
                }
            }, label@{
                return@label returnPropertyIfSame(hasAlphas) { it.alpha.value }.toString()
            }, Float::class.java, "Set alpha set point"))
            inputElements.add(CheckBoxInput(null, { input ->
                for (element in hasAlphas) {
                    element.alpha.interpolated = input!!
                }
            },
                label@{ return@label returnPropertyIfSame(hasAlphas) { it.alpha.interpolated } ?: false },
                "Interpolate Alpha"))
        }

        if (hasColors.isNotEmpty()) {
            inputElements.add(TextInput(null, { input ->
                if (input != null && input != "") {
                    val colorWrapper = ColorWrapper.parseString(input)
                    if (colorWrapper != null) {
                        for (element in hasColors) {
                            element.color.value = colorWrapper
                            element.color.newSetPoint(time, colorWrapper)
                        }
                    }
                } else {
                    for (element in hasColors) {
                        element.color.removeFrame(time)
                    }
                }
            }, label@{
                return@label returnPropertyIfSame(hasColors) { it.color.value.color }.toString().dropLast(2) // remove unused a from rgba string
            }, String::class.java, "Set color"))
            inputElements.add(CheckBoxInput(null, { input ->
                for (element in hasColors) {
                    element.color.interpolated = input!!
                }
            },
                label@{ return@label returnPropertyIfSame(hasColors) { it.color.interpolated } ?: false },
                "Interpolate Color"))
        }

        @Suppress("UNCHECKED_CAST")
        for (specificClass in classElementMap.keys) {
            if (specificClass == Arrow::class.java) {
                inputElements.addAll(getArrowInputs(classElementMap[specificClass] as List<Arrow>))
            }
            if (specificClass == Image::class.java) {
                inputElements.addAll(getImageInputs(classElementMap[specificClass] as List<Image>))
            }
            if (specificClass == Label::class.java) {
                inputElements.addAll(getMapLabelInputs(classElementMap[specificClass] as List<Label>))
            }
            if (specificClass == Unit::class.java) {
                inputElements.addAll(getUnitInputs(classElementMap[specificClass] as List<Unit>))
            }
            if (specificClass == Node::class.java) {
                inputElements.addAll(getNodeInputs(classElementMap[specificClass] as List<Node>, time))
            }
            if (specificClass == NodeCollection::class.java) {
                inputElements.addAll(getNodeCollectionInputs(classElementMap[specificClass] as List<NodeCollection>))
            }
        }

        inputElements.forEach { it.show(verticalGroup, skin) }
    }

    private fun getArrowInputs(arrows: List<Arrow>) : List<InputElement<*>> {
        return listOf(
            TextInput(null, { input ->
                if (input != null) {
                    for (arrow in arrows) {
                        arrow.thickness = input
                    }
                }
            }, label@{
                return@label returnPropertyIfSame(arrows) { it.thickness }.toString()
            }, Float::class.java, "Set thickness")
        )
    }

    private fun getImageInputs(images: List<Image>) : List<InputElement<*>> {
        return listOf(
            SelectBoxInput(null, { input ->
                for (image in images) {
                    image.texture.key = input ?: ""
                    image.texture.loadTexture(externalLoader)
                }
            }, label@{
                return@label returnPropertyIfSame(images) { it.texture.key }
            }, String::class.java, "Image", Array<String>().apply {
                add("")
                externalLoader.loadedImages.keys.forEach { add(it) }
            }),
            TextInput(null, { input ->
                if (input != null) {
                    if (input >= 0) {
                        for (image in images) {
                            image.scale = input
                        }
                    }
                }
            }, label@{
                return@label returnPropertyIfSame(images) { it.scale }.toString()
            }, Float::class.java, "Set scale")
            )
    }

    private fun getMapLabelInputs(labels: List<Label>) : List<InputElement<*>> {
        return listOf(
            TextInput(null, { input ->
                if (input != null) {
                    if (input > 0) {
                        for (mapLabel in labels) {
                            mapLabel.size = input
                        }
                    }
                }
            }, label@{
                return@label returnPropertyIfSame(labels) { it.size }.toString()
            }, Float::class.java, "Set size"),
            TextInput(null, { input ->
                for (mapLabel in labels) {
                    mapLabel.text = input ?: ""
                }
            }, label@{
                return@label returnPropertyIfSame(labels) { it.text }
            }, String::class.java, "Set text")
        )
    }

    private fun getUnitInputs(units: List<Unit>) : List<InputElement<*>> {
        return listOf(
            TextInput(null, { input ->
                for (unit in units) {
                    unit.size = input?: ""
                }
            }, label@{
                return@label returnPropertyIfSame(units) { it.size }
            }, String::class.java, "Set size"),
            TextInput(null, { input ->
                for (unit in units) {
                    unit.drawSize = if (input != null && input != 0f) {
                        input
                    } else {
                        null
                    }
                }
            }, label@{
                return@label returnPropertyIfSame(units) { it.drawSize }.toString()
            }, Float::class.java, "Set draw size"),
            SelectBoxInput(null, { input ->
                if (input != null && input != "") {
                    if (input in InternalLoader.listChildren(InternalLoader.DEFAULT_SYMBOLS)) {
                        for (unit in units) {
                            unit.type.key = InternalLoader.defaultSymbols(input)
                            unit.type.loadTexture(null)
                        }
                    } else {
                        for (unit in units) {
                            unit.type.key = input
                            unit.type.loadTexture(externalLoader)
                        }
                    }
                } else {
                    for (unit in units) {
                        unit.type.key = ""
                    }
                }
            }, label@{
                return@label returnPropertyIfSame(units) { it.type.key }?.removePrefix(InternalLoader.DEFAULT_SYMBOLS)
            }, String::class.java, "Set type",
                gdxArrayOf(InternalLoader.listChildren(InternalLoader.DEFAULT_SYMBOLS)).apply {
                addAll(gdxArrayOf(externalLoader.loadedImages.keys))
                    add("")
            }),
            SelectBoxInput(null, { input ->
                for (unit in units) {
                    unit.country.key = input?: ""
                    unit.country.loadTexture(externalLoader)
                }
            }, label@{
                return@label returnPropertyIfSame(units) { it.country.key }
            }, String::class.java, "Set country", gdxArrayOf(externalLoader.loadedImages.keys).apply {
                add("")
            }),
            TextInput(null, { input ->
                for (unit in units) {
                    unit.name = input ?: ""
                }
            }, label@{
                return@label returnPropertyIfSame(units) { it.name }
            }, String::class.java, "Set name")
        )
    }

    private fun getNodeInputs(nodes: List<Node>, time: Int) : List<InputElement<*>> {
        val inputs = mutableListOf<InputElement<Double>>()

        for (node in nodes) {
            for (parent in node.parents) {
                if (time == parent.first) {
                    inputs.add(TextInput(null, { input ->
                        if (input != null) {
                            if (node.tSetPoints.setPoints[time] == null) {
                                node.tSetPoints.setPoints[time] = mutableMapOf()
                            }
                            node.tSetPoints.setPoints[time]!![parent.second.value] = input
                        } else {
                            node.tSetPoints.setPoints[time]?.remove(parent.second.value)
                        }
                    }, label@{
                        return@label node.tSetPoints.setPoints[time]?.get(parent.second.value).toString()
                    }, Double::class.java, "Set t set point for node on NC ${parent.second.value}"))
                }
            }
        }
        animation.nodeEdgeHandler.updateNodeCollections()
        return inputs
    }

    private fun getNodeCollectionInputs(nodeCollections: List<NodeCollection>) : List<InputElement<*>> {
        return listOf(
            SelectBoxInput(null, { input ->
                for (nodeCollection in nodeCollections) {
                    nodeCollection.type = input ?: "None"
                    if (nodeCollection.type == "Line") {
                        nodeCollection.width = 5f
                        nodeCollection.order = "c"
                    } else {
                        nodeCollection.order = "e"
                    }
                }
            }, label@{
                return@label returnPropertyIfSame(nodeCollections) { it.type }
            }, String::class.java, "Set node collection type", Array<String>().apply { add("Area", "Line") })
        )
    }

    private fun <I, O>returnPropertyIfSame(things : List<I>, getProperty : ((I) -> O)) : O? {
        if (things.isEmpty()) {
            return null
        }
        val first = getProperty(things.first())
        for (thing in things) {
            if (first != getProperty(thing)) {
                return null
            }
        }
        return first
    }
}
