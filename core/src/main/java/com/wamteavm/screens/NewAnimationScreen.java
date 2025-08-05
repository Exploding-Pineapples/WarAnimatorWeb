package com.wamteavm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.wamteavm.WarAnimator;
import com.wamteavm.files.FileHandler;
import com.wamteavm.models.Animation;
import kotlin.Pair;

import static com.badlogic.gdx.Gdx.gl;
import static com.wamteavm.WarAnimator.DISPLAY_HEIGHT;
import static com.wamteavm.WarAnimator.DISPLAY_WIDTH;


public class NewAnimationScreen extends ScreenAdapter implements InputProcessor {
    WarAnimator game;
    public Stage stage = new Stage();
    Table table = new Table();
    public Label warningLabel;

    public NewAnimationScreen(WarAnimator game, Animation animation) {
        this.game = game;

        Table titleTable = new Table();
        titleTable.setPosition(DISPLAY_WIDTH / 2f, DISPLAY_HEIGHT - 100);
        Label titleLabel;
        if (FileHandler.INSTANCE.getAnimations().contains(animation)) {
            titleLabel = new Label("Editing " + animation.getName(), game.skin);
        } else {
            titleLabel = new Label("Creating new animation", game.skin);
        }
        titleTable.add(titleLabel);
        stage.addActor(titleTable);

        table.setPosition(DISPLAY_WIDTH / 2f, DISPLAY_HEIGHT / 2f);

        TextButton menuButton = new TextButton("Menu", game.skin, "small");
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.input.setInputProcessor(game.menuScreen.stage);
                game.setScreen(game.menuScreen); //No need to reinitialize the menu since it's not possible to have added an animation
            }
        });
        menuButton.setPosition(100, 100);
        stage.addActor(menuButton);

        Table nameArea = new Table();
        Label nameLabel = new Label("Name: ", game.skin);
        TextField nameField = new TextField(animation.getName(), game.skin);
        nameArea.add(nameLabel);
        nameArea.add(nameField);
        nameArea.row();
        table.add(nameArea);
        table.row().pad(10);

        TextButton submitButton = new TextButton("Submit", game.skin, "small");
        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean animationExists = false;
                String name = nameField.getText();
                Pair<Boolean, String> inputCheck = checkInput(nameField.getText());
                if (inputCheck.getFirst()) {
                    for (Animation existingAnimation : FileHandler.INSTANCE.getAnimations()) {
                        if (existingAnimation.getName().equals(name)) {
                            System.out.println("Using existing animation: " + name);
                            existingAnimation.setName(nameField.getText());
                            animationExists = true;

                            game.setScreen(new LoadingScreen(game, existingAnimation));
                            break;
                        }
                    }
                    if (!animationExists) {
                        System.out.println("Created New Animation");
                        Animation newAnimation = new Animation(nameField.getText());
                        FileHandler.INSTANCE.createNewAnimation(newAnimation);
                        game.setScreen(new LoadingScreen(game, newAnimation));
                    }
                } else {
                    warningLabel.setText(inputCheck.getSecond());
                }
            }
        });

        table.add(submitButton).height(40);
        table.row().pad(10);

        warningLabel = new Label("", game.skin);
        warningLabel.setColor(Color.RED);
        table.add(warningLabel);

        init();
    }

    public Pair<Boolean, String> checkInput(String name) {
        if (name.isEmpty()) {
            return new Pair<>(false, "Name cannot be empty");
        }
        return new Pair<>(true, "");
    }

    public void init() {
        Gdx.input.setInputProcessor(stage);
        stage.addActor(table);
    }
    @Override
    public void render(float delta) {
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
