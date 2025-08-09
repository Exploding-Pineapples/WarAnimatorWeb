package com.wamteavm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.wamteavm.WarAnimator;

import static com.badlogic.gdx.Gdx.gl;
import static com.wamteavm.WarAnimator.DISPLAY_WIDTH;

public class LoginScreen implements Screen {
    Stage stage;
    Table table;
    WarAnimator game;

    public LoginScreen(WarAnimator game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage();
        table = new Table();
        table.setFillParent(true);

        Label title = new Label("Login / Register", game.skin);
        table.add(title);
        table.row().pad(10f);

        TextField username = new TextField("", game.skin);
        username.setMessageText("Enter a username");
        table.add(username).width(DISPLAY_WIDTH / 4f);
        table.row().pad(10f);

        TextField password = new TextField("", game.skin);
        password.setMessageText("Enter a password");
        table.add(password).width(DISPLAY_WIDTH / 4f);
        table.row().pad(10f);

        Label warningLabel = new Label("", game.skin);
        warningLabel.setColor(new Color(1f, 0f, 0f, 1f));
        table.add(warningLabel);
        table.row();

        TextButton submit = getTextButton(username, password, warningLabel);
        table.add(submit).height(50f);
        table.row();

        stage.addActor(table);

        Gdx.input.setInputProcessor(stage);
    }

    private TextButton getTextButton(TextField username, TextField password, Label warningLabel) {
        TextButton submit = new TextButton("Submit", game.skin, "small");
        submit.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                switch (checkInput(username.getText(), password.getText())) {
                    case 0:
                        game.menuScreen = new MenuScreen(game);
                        game.setScreen(game.menuScreen);
                        dispose();
                        return;
                    case 1:
                        warningLabel.setText("Enter a username");
                        return;
                    case 2:
                        warningLabel.setText("Enter a password");
                }
            }
        });
        return submit;
    }

    private int checkInput(String username, String password) {
        if (username.isEmpty()) {
            return 1;
        }
        if (password.isEmpty()) {
            return 2;
        }
        return 0;
    }

    @Override
    public void render(float delta) {
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
    }
}
