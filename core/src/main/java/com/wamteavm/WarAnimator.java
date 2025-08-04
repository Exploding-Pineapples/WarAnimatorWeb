package com.wamteavm;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.wamteavm.files.Assets;
import com.wamteavm.screens.MenuScreen;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class WarAnimator extends Game {
    public Skin skin;
    public BitmapFont bitmapFont;
    public ShaderProgram fontShader;
    public SpriteBatch batch;
    public ShapeDrawer shapeDrawer;
    public MenuScreen menuScreen;
    public InputMultiplexer multiplexer;

    @Override
    public void create() {
        skin = Assets.INSTANCE.loadSkin("skin/glassy-ui.json");
        bitmapFont = Assets.INSTANCE.loadFont();
        fontShader = Assets.INSTANCE.loadFontShader();
        batch = new SpriteBatch();
        shapeDrawer = new ShapeDrawer(batch, Assets.INSTANCE.whitePixel());
        multiplexer = new InputMultiplexer();

        menuScreen = new MenuScreen(this);
        setScreen(menuScreen);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    public static final int DISPLAY_WIDTH = 1920;
    public static final int DISPLAY_HEIGHT = 1080;
}
