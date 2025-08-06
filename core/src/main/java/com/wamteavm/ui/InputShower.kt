package com.wamteavm.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Array
import com.wamteavm.files.Assets
import com.wamteavm.ui.inputelements.InputElement
import com.wamteavm.ui.inputelements.TextInput
import com.wamteavm.models.*
import com.wamteavm.models.screenobjects.Arrow
import com.wamteavm.models.screenobjects.Image
import com.wamteavm.models.screenobjects.MapLabel
import com.wamteavm.models.screenobjects.Unit
import com.wamteavm.ui.inputelements.SelectBoxInput
import com.wamteavm.utilities.AreaColor

class InputShower(val skin: Skin) {
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
        val elements = objects.filter { HasInputs::class.java.isAssignableFrom(it.javaClass) } as List<HasInputs>
        var alpha = elements.isNotEmpty()
        var color = elements.isNotEmpty()
        var specificClass: Class<HasInputs>? = HasInputs::class.java

        for (element in elements) {
            if (alpha && !HasAlpha::class.java.isAssignableFrom(element.javaClass)) {
                alpha = false
            }
            if (color && !HasColor::class.java.isAssignableFrom(element.javaClass)) {
                color = false
            }
            if (specificClass != null) {
                specificClass = if (specificClass.isAssignableFrom(element.javaClass)) {
                    element.javaClass
                } else {
                    null
                }
            }
        }

        if (alpha) {
            val alphaInput = TextInput(null, { input ->
                if (input != null) {
                    for (element in elements) {
                        (element as HasAlpha).alpha.newSetPoint(time, input)
                        element.alpha.value = input
                    }
                } else {
                    for (element in elements) {
                        (element as HasAlpha).alpha.removeFrame(time)
                    }
                }
            }, label@{
                return@label returnIfSame(elements.map { (it as HasAlpha).alpha.value.toString() })
            }, Float::class.java, "Set alpha set point")
            inputElements.add(alphaInput)
        }

        if (color) {
            val colorInput = TextInput(null, { input ->
                if (input != null) {
                    for (areaColor in AreaColor.entries) {
                        if (input == areaColor.name) {
                            for (element in elements) {
                                (element as HasColor).color = areaColor
                            }
                        }
                    }
                }
            }, label@{
                return@label returnIfSame(elements.map { (it as HasColor).color.toString() })
            }, String::class.java, "Set color")
            inputElements.add(colorInput)
        }

        @Suppress("UNCHECKED_CAST")
        if (specificClass != null) {
            if (specificClass == Arrow::class.java) {
                inputElements.addAll(getArrowInputs(elements as List<Arrow>))
            }
            if (specificClass == Image::class.java) {
                inputElements.addAll(getImageInputs(elements as List<Image>))
            }
            if (specificClass == MapLabel::class.java) {
                inputElements.addAll(getMapLabelInputs(elements as List<MapLabel>))
            }
            if (specificClass == Unit::class.java) {
                inputElements.addAll(getUnitInputs(elements as List<Unit>))
            }
            if (specificClass == Node::class.java) {
                inputElements.addAll(getNodeInputs(elements as List<Node>))
            }
            if (specificClass == NodeCollection::class.java) {
                inputElements.addAll(getNodeCollectionInputs(elements as List<NodeCollection>))
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
                return@label returnIfSame(arrows.map { it.thickness.toString() })
            }, Float::class.java, "Set thickness")
        )
    }

    fun getImageInputs(images: List<Image>) : List<InputElement<*>> {
        return listOf(
            SelectBoxInput(null, { input ->
                for (image in images) {
                    image.updateTexture(Assets.mapsPath(input ?: ""))
                }
            }, label@{
                return@label returnIfSame(images.map { it.path.substringAfter("assets/maps/") })
            }, String::class.java, "Image", Assets.images()),
            TextInput(null, { input ->
                if (input != null) {
                    if (input >= 0) {
                        for (image in images) {
                            image.scale = input
                        }
                    }
                }
            }, label@{
                return@label returnIfSame(images.map { it.scale.toString() })
            }, Float::class.java, "Set scale")
            )
    }

    fun getMapLabelInputs(mapLabels: List<MapLabel>) : List<InputElement<*>> {
        return listOf(
            TextInput(null, { input ->
                if (input != null) {
                    if (input > 0) {
                        for (mapLabel in mapLabels) {
                            mapLabel.size = input
                        }
                    }
                }
            }, label@{
                return@label returnIfSame(mapLabels.map { it.size.toString() })
            }, Float::class.java, "Set size"),
            TextInput(null, { input ->
                for (mapLabel in mapLabels) {
                    mapLabel.text = input ?: ""
                }
            }, label@{
                return@label returnIfSame(mapLabels.map { it.text })
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
                return@label returnIfSame(units.map { it.size })
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
                return@label returnIfSame(units.map { it.drawSize.toString() })
            }, Float::class.java, "Set draw size"),
            SelectBoxInput(null, { input ->
                if (input != null) {
                    for (unit in units) {
                        unit.type = input
                        unit.updateTypeTexture()
                    }
                }
            }, label@{
                return@label returnIfSame(units.map { it.type })
            }, String::class.java, "Set type", Assets.unitTypes()),
            SelectBoxInput(null, { input ->
                for (unit in units) {
                    unit.image = Assets.flagsPath(input ?: "")
                    unit.updateCountryTexture()
                }
            }, label@{
                return@label returnIfSame(units.map { it.image.substringAfter("assets/flags/") })
            }, String::class.java, "Set country", Assets.countryNames),
            TextInput(null, { input ->
                for (unit in units) {
                    unit.name = input ?: ""
                }
            }, label@{
                return@label returnIfSame(units.map { it.name })
            }, String::class.java, "Set name")
        )
    }

    fun getNodeInputs(nodes: List<Node>) : List<InputElement<*>> {
        return listOf(
            TextInput(null, { input ->
                for (node in nodes) {
                    node.tSetPoint = input
                }
            }, label@{
                return@label returnIfSame(nodes.map { it.tSetPoint.toString() })
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
                    }
                }
            }, label@{
                return@label returnIfSame(nodeCollections.map { it.type })
            }, String::class.java, "Set node collection type", Array<String>().apply { add("Area", "Line") })
        )
    }

    private fun returnIfSame(strings : List<String>) : String {
        if (strings.isEmpty()) {
            return ""
        }
        val first = strings.first()
        for (string in strings) {
            if (first != string) {
                return ""
            }
        }
        return first
    }
}
