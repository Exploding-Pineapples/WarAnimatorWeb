package com.wamteavm.screens;

import com.badlogic.gdx.Gdx;
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
import com.wamteavm.models.Animation;
import kotlin.Pair;

import static com.badlogic.gdx.Gdx.gl;
import static com.wamteavm.WarAnimator.DISPLAY_HEIGHT;
import static com.wamteavm.WarAnimator.DISPLAY_WIDTH;


public class EditAnimationScreen extends ScreenAdapter {
    WarAnimator game;
    Stage stage = new Stage();
    Table table = new Table();
    Label warningLabel;
    TextButton uploadImage;
    boolean newAnimation;

    public EditAnimationScreen(WarAnimator game, Animation animation, boolean newAnimation) {
        this.game = game;
        this.newAnimation = newAnimation;

        table.setPosition(DISPLAY_WIDTH / 2f, DISPLAY_HEIGHT / 2f);

        Table titleTable = new Table();
        titleTable.setPosition(DISPLAY_WIDTH / 2f, DISPLAY_HEIGHT - 100);
        Label titleLabel = new Label("", game.skin, "big");
        if (newAnimation) {
            titleLabel.setText("Creating new animation");
        } else {
            titleLabel.setText("Editing " + animation.getName());
        }
        titleTable.add(titleLabel);
        stage.addActor(titleTable);

        Table nameArea = new Table();
        Label nameLabel = new Label("Name: ", game.skin);
        TextField nameField = new TextField(animation.getName(), game.skin);
        nameArea.add(nameLabel);
        nameArea.add(nameField);
        table.add(nameArea);
        table.row().pad(10);

        table.add(new Label("Images:", game.skin));
        table.row();

        Table imagesTable = new Table();

        uploadImage = new TextButton("Upload Image", game.skin, "small");
        uploadImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.loader.addImage(animation, () -> {updateImages(animation, game, imagesTable); return null;});
            }
        });
        table.add(uploadImage).height(50).pad(10);
        table.row();

        updateImages(animation, game, imagesTable);

        table.add(imagesTable);
        table.row().pad(10);

        TextButton submitButton = new TextButton("Submit", game.skin, "small");
        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Pair<Boolean, String> inputCheck = checkInput(nameField.getText());
                if (inputCheck.getFirst()) {
                    animation.setName(nameField.getText());
                    if (newAnimation) {
                        game.loader.addAnimation(animation);
                    }
                    game.setScreen(new LoadingScreen(game));
                    game.setScreen(new AnimationScreen(game, animation));
                } else {
                    warningLabel.setText(inputCheck.getSecond());
                }
            }
        });

        table.add(submitButton).height(50);
        table.row().pad(10);

        warningLabel = new Label("", game.skin);
        warningLabel.setColor(Color.RED);
        table.add(warningLabel);

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

        stage.addActor(table);
    }

    public Pair<Boolean, String> checkInput(String name) {
        if (name.isEmpty()) {
            return new Pair<>(false, "Name cannot be empty");
        }
        if (newAnimation) {
            for (Animation existing : game.loader.getAnimations()) {
                if (existing.getName().equals(name)) {
                    return new Pair<>(false, "Animation already exists");
                }
            }
        }
        return new Pair<>(true, "");
    }

    private void updateImages(Animation animation, WarAnimator game, Table imagesTable) {
        imagesTable.clear();
        game.loader.loadImages(animation);
        for (String image : game.loader.getLoadedImages().keySet().stream().toList()) { // Copy the keys to avoid concurrent modification
            Table imageTable = new Table();
            imageTable.add(new Label(image, game.skin)).pad(10);
            TextButton deleteButton = new TextButton("Delete", game.skin, "small");
            deleteButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    animation.getImageKeys().remove(image);
                    imagesTable.removeActor(imageTable);
                    game.loader.saveAnimations();
                }
            });

            imageTable.add(deleteButton).height(45);
            imagesTable.add(imageTable);
            imagesTable.row();
        }
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override
    public void render(float delta) {
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }
}
