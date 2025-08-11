package com.wamteavm.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.wamteavm.WarAnimator
import com.wamteavm.loaders.externalloaders.APIExternalLoader.api

class LoginScreen(var game: WarAnimator) : ScreenAdapter(), InputProcessor {
    var stage: Stage? = null
    var table: Table? = null

    val username: TextField by lazy { TextField("", game.skin) }
    val password: TextField by lazy { TextField("", game.skin) }
    val warningLabel: Label by lazy { Label("", game.skin) }

    override fun show() {
        val loginButton = TextButton("Login / Register", game.skin, "small")

        stage = Stage()
        table = Table()
        table!!.setFillParent(true)

        val title = Label("Login / Register", game.skin)
        table!!.add(title)
        table!!.row().pad(10f)

        username.messageText = "Enter a username"
        table!!.add(username).width(WarAnimator.DISPLAY_WIDTH / 4f)
        table!!.row().pad(10f)

        password.messageText = "Enter a password"
        table!!.add(password).width(WarAnimator.DISPLAY_WIDTH / 4f)
        table!!.row().pad(10f)

        warningLabel.color = Color(1f, 0f, 0f, 1f)
        table!!.add(warningLabel)
        table!!.row()

        loginButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                submit()
            }
        })
        table!!.add(loginButton).height(50f)
        table!!.row()

        stage!!.addActor(table)

        Gdx.input.inputProcessor = stage
    }

    private fun submit() {
        val result = checkInput(username.text, password.text)
        if (result.first) {
            game.menuScreen = MenuScreen(game)
            game.screen = game.menuScreen
            dispose()
        } else {
            warningLabel.setText(result.second)
        }
    }

    private fun checkInput(username: String, password: String): Pair<Boolean, String> {
        if (username.isEmpty()) {
            return Pair(false, "Username cannot be empty")
        }
        if (password.isEmpty()) {
            return Pair(false, "Password cannot be empty")
        }

        val loginResult = runCatching { api.login(username, password) }
        println("login result: $loginResult")
        if (loginResult.isFailure) {
            val registerResult = runCatching { api.register(username, password) }
            if (registerResult.isFailure) {
                return Pair(false, registerResult.toString())
            }
        }
        return Pair(true, "")
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage!!.act(delta)
        stage!!.draw()
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
            submit()
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return true
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return true
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        return true
    }
}
