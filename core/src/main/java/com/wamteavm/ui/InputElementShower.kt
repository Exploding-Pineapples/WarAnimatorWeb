package com.wamteavm.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Array
import com.wamteavm.loaders.InternalLoader
import com.wamteavm.ui.inputelements.InputElement
import com.wamteavm.ui.inputelements.TextInput
import com.wamteavm.models.*
import com.wamteavm.models.screenobjects.Arrow
import com.wamteavm.models.screenobjects.Image
import com.wamteavm.models.screenobjects.Label
import com.wamteavm.models.screenobjects.Unit
import com.wamteavm.ui.inputelements.CheckBoxInput
import com.wamteavm.ui.inputelements.SelectBoxInput
import com.wamteavm.utilities.ColorWrapper

class InputElementShower(val skin: Skin, val animation: Animation) {
    val inputElements: MutableList<InputElement<*>> = mutableListOf()

    fun hideAll(verticalGroup: VerticalGroup) {
        inputElements.forEach { it.hide(verticalGroup) }
    }

    fun showAll(verticalGroup: VerticalGroup) {
        inputElements.forEach { it.show(verticalGroup, skin) }
    }

    fun update(verticalGroup: VerticalGroup, objects: List<AnyObject>, time: Int) {
        inputElements.forEach { it.hide(verticalGroup) }
        inputElements.clear()
        val hasInputs = objects.filter { HasInputs::class.java.isAssignableFrom(it.javaClass) } as List<HasInputs>

        val hasAlphas: MutableList<HasAlpha> = mutableListOf()
        val hasColors: MutableList<HasColor> = mutableListOf()
        val drawables: MutableList<Drawable> = mutableListOf()
        val classElementMap: MutableMap<Class<HasInputs>, MutableList<HasInputs>> = mutableMapOf()

        for (element in hasInputs) {
            if (HasAlpha::class.java.isAssignableFrom(element.javaClass)) {
                hasAlphas.add(element as HasAlpha)
            }
            if (HasColor::class.java.isAssignableFrom(element.javaClass)) {
                hasColors.add(element as HasColor)
            }
            if (Drawable::class.java.isAssignableFrom(element.javaClass)) {
                drawables.add(element as Drawable)
            }
            if (classElementMap.containsKey(element.javaClass)) {
                classElementMap[element.javaClass]!!.add(element)
            } else {
                classElementMap[element.javaClass] = mutableListOf(element)
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
                    println(input)
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
                inputElements.addAll(getNodeInputs(classElementMap[specificClass] as List<Node>))
            }
            if (specificClass == NodeCollection::class.java) {
                inputElements.addAll(getNodeCollectionInputs(classElementMap[specificClass] as List<NodeCollection>))
            }
        }

        inputElements.forEach { it.show(verticalGroup, skin) }
    }

    fun getArrowInputs(arrows: List<Arrow>) : List<InputElement<*>> {
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

    fun getImageInputs(images: List<Image>) : List<InputElement<*>> {
        return listOf(
            SelectBoxInput(null, { input ->
                for (image in images) {
                    image.updateTexture(InternalLoader.mapsPath(input ?: ""))
                }
            }, label@{
                return@label returnPropertyIfSame(images) { it.path.substringAfter("assets/maps/") }
            }, String::class.java, "Image", InternalLoader.images()),
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

    fun getMapLabelInputs(labels: List<Label>) : List<InputElement<*>> {
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

    fun getUnitInputs(units: List<Unit>) : List<InputElement<*>> {
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
                if (input != null) {
                    for (unit in units) {
                        unit.type = input
                        unit.updateTypeTexture()
                    }
                }
            }, label@{
                return@label returnPropertyIfSame(units) { it.type }
            }, String::class.java, "Set type", InternalLoader.unitTypes()),
            SelectBoxInput(null, { input ->
                for (unit in units) {
                    unit.country = InternalLoader.flagsPath(input ?: "")
                    unit.updateCountryTexture()
                }
            }, label@{
                return@label returnPropertyIfSame(units) { it.country }?.substringAfter("assets/flags/")
            }, String::class.java, "Set country", InternalLoader.countryNames),
            TextInput(null, { input ->
                for (unit in units) {
                    unit.name = input ?: ""
                }
            }, label@{
                return@label returnPropertyIfSame(units) { it.name }
            }, String::class.java, "Set name")
        )
    }

    fun getNodeInputs(nodes: List<Node>) : List<InputElement<*>> {
        return listOf(
            TextInput(null, { input ->
                for (node in nodes) {
                    node.tSetPoint = input
                }
                animation.nodeEdgeHandler.updateNodeCollections()
            }, label@{
                return@label returnPropertyIfSame(nodes) { it.tSetPoint }.toString()
            }, Double::class.java, "Set t set point")
        )
    }

    fun getNodeCollectionInputs(nodeCollections: List<NodeCollection>) : List<InputElement<*>> {
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
