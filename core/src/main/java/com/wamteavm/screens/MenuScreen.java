package com.wamteavm.screens;

import com.badlogic.gdx.Gdx;
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
import com.wamteavm.models.Animation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.wamteavm.WarAnimator.DISPLAY_HEIGHT;
import static com.wamteavm.WarAnimator.DISPLAY_WIDTH;

public class MenuScreen extends ScreenAdapter {
    WarAnimator game;
    Skin skin;
    Stage stage;
    Table table;

    public MenuScreen(WarAnimator game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage();
        table = new Table();
        skin = game.skin;
        table.setPosition(DISPLAY_WIDTH/2f, DISPLAY_HEIGHT/2f);

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

        stage.addActor(table);

        Gdx.input.setInputProcessor(stage);

        updateAnimations();
    }

    private @NotNull TextButton getOpenButton(Animation animation) {
        TextButton textButton = new TextButton("Open", skin, "small");
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LoadingScreen(game, () -> game.setScreen(new AnimationScreen(game, animation))));
            }
        });
        return textButton;
    }

    private @NotNull TextButton getDeleteButton(Animation animation) {
        TextButton textButton = new TextButton("Delete", skin, "small");
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.loader.deleteAnimation(animation);
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
                game.setScreen(new LoadingScreen(game, () -> game.setScreen(new EditAnimationScreen(game, animation, false))));
            }
        });
        return textButton;
    }

    public void updateAnimations() {
        List<Animation> animations = game.loader.getAnimations();

        Label title = new Label("", skin);
        if (animations.isEmpty()) {
            title.setText("You have no animations.");
        } else {
            title.setText("Animations:");
        }
        table.add(title).colspan(4);
        table.row().pad(10).height(45);

        for (Animation animation : animations) {
            Label nameLabel = new Label(animation.getName(), skin);

            table.add(getDeleteButton(animation));
            table.add(nameLabel);
            table.add(getOpenButton(animation));
            table.add(getEditButton(animation));

            table.row().pad(10).height(45);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }
}
