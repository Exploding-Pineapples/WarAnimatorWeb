package com.wamteavm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.wamteavm.WarAnimator;

import static com.wamteavm.WarAnimator.DISPLAY_HEIGHT;
import static com.wamteavm.WarAnimator.DISPLAY_WIDTH;

public class LoadingScreen extends ScreenAdapter {
    WarAnimator game;
    GlyphLayout layout;

    public LoadingScreen(WarAnimator game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        game.bitmapFont.setColor(new Color(1f, 1f, 1f, 1f));
        game.bitmapFont.getData().setScale(1f);

        layout = new GlyphLayout();
        layout.setText(game.bitmapFont, "Loading...");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        game.batch.setShader(game.fontShader);
        game.fontShader.setUniformf("scale", 1.0f);
        game.fontShader.setUniformf("outlineDistance", 0.5f); // See assets/font.frag

        game.bitmapFont.draw(game.batch, layout, DISPLAY_WIDTH/2F - layout.width / 2f, DISPLAY_HEIGHT/2F - layout.height / 2f);
        game.batch.end();
    }

    @Override
    public void hide() {
        game.batch.setShader(null);
    }
}
