package com.wamteavm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.wamteavm.WarAnimator;
import com.wamteavm.files.FileHandler;
import com.wamteavm.models.Animation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.wamteavm.WarAnimator.DISPLAY_HEIGHT;
import static com.wamteavm.WarAnimator.DISPLAY_WIDTH;

public class MenuScreen extends ScreenAdapter implements InputProcessor {
    WarAnimator game;
    Skin skin;
    Stage stage = new Stage();
    Table table = new Table();

    public MenuScreen(WarAnimator game) {
        skin = game.skin;
        this.game = game;
        table.setPosition(DISPLAY_WIDTH/2f, DISPLAY_HEIGHT/2f);

        TextButton logout = new TextButton("Logout", skin, "small");
        logout.setPosition(100f, 100f);
        logout.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LoginScreen(game));
                dispose();
            }
        });
        stage.addActor(logout);

        Table titleTable = new Table();
        titleTable.setPosition(DISPLAY_WIDTH/2f, DISPLAY_HEIGHT - 100);
        Label titleLabel = new Label("War Animator", skin);
        titleLabel.setPosition(DISPLAY_WIDTH/2f, DISPLAY_HEIGHT - 100);
        titleTable.add(titleLabel);
        stage.addActor(titleTable);

        TextButton newAnimationButton = new TextButton("New Animation", skin, "small");
        newAnimationButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new EditAnimationScreen(game, new Animation(), true));
                dispose();
            }
        });
        table.add(newAnimationButton).colspan(4);
        table.row().pad(10);

        FileHandler.INSTANCE.load();
        List<Animation> animations = FileHandler.INSTANCE.getAnimations();

        Label title = new Label("", skin);
        if (animations.isEmpty()) {
            title.setText("You have no animations.");
        } else {
            title.setText("Animations:");
        }
        table.add(title).colspan(4);
        table.row().pad(10).height(40);

        for (Animation animation : animations) {
            Label nameLabel = new Label(animation.getName(), skin);

            table.add(getDeleteButton(animation));
            table.add(nameLabel);
            table.add(getOpenButton(animation));
            table.add(getEditButton(animation));

            table.row().pad(10).height(40);
        }

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    private @NotNull TextButton getOpenButton(Animation animation) {
        TextButton textButton = new TextButton("Open", skin, "small");
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                LoadingScreen loadingScreen = new LoadingScreen(game, animation);
                game.setScreen(loadingScreen);
            }
        });
        return textButton;
    }

    private @NotNull TextButton getDeleteButton(Animation animation) {
        TextButton textButton = new TextButton("Delete", skin, "small");
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                FileHandler.INSTANCE.deleteAnimation(animation);
                game.menuScreen = new MenuScreen(game);
                game.setScreen(game.menuScreen);
            }
        });
        return textButton;
    }

    private @NotNull TextButton getEditButton(Animation animation) {
        TextButton textButton = new TextButton("Edit", skin, "small");
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                EditAnimationScreen editAnimationScreen = new EditAnimationScreen(game, animation, false);
                game.setScreen(editAnimationScreen);
            }
        });
        return textButton;
    }

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int b) {
        return true;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float v, float v1) {
        return false;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
}
