package com.wamteavm.ui.inputelements

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener

class CheckBoxInput(skin: Skin?, output: (Boolean?) -> Unit, val input: () -> Boolean, name: String) : InputElement<Boolean>(skin, output, Boolean::class.java, name, { string -> string.toBoolean() }) {
    @Transient override var inputElement: Actor? = null

    override fun show(verticalGroup: VerticalGroup, inSkin: Skin) {
        if (!displayed) {
            val skin: Skin = inSkin
            table = Table()
            val checkBox = CheckBox(name, skin)
            checkBox.isChecked = input.invoke()
            inputElement = checkBox
            checkBox.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    output.invoke(checkBox.isChecked)
                }
            })

            table!!.add(checkBox).pad(10.0f)
            table!!.row()

            verticalGroup.addActor(table)
            displayed = true
        }
    }
}
