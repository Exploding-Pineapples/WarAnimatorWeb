package com.wamteavm;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.wamteavm.loaders.externalloaders.AbstractExternalLoader;
import com.wamteavm.loaders.InternalLoader;
import com.wamteavm.loaders.externalloaders.APIExternalLoader;
import com.wamteavm.loaders.externalloaders.BrowserIO;
import com.wamteavm.loaders.externalloaders.FileExternalLoader;
import com.wamteavm.models.Animation;
import com.wamteavm.screens.AnimationScreen;
import com.wamteavm.screens.LoginScreen;
import com.wamteavm.screens.MenuScreen;
import com.waranimator.api.client.models.AuthResult;
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
    public AbstractExternalLoader animationLoader;
    private final Screen firstScreen;
    public boolean web;

    public WarAnimator(boolean web) {
        this.web = web;
        animationLoader = web ? APIExternalLoader.INSTANCE : FileExternalLoader.INSTANCE;
        firstScreen = web ? new LoginScreen(this) : new MenuScreen(this);
    }

    public WarAnimator(AuthResult authResult, Animation animation) { // Skip login, go directly to AnimationScreen. Only from direct edit animation link
        web = true;
        APIExternalLoader.INSTANCE.getApi().setAuthToken(authResult.getToken());
        animationLoader = APIExternalLoader.INSTANCE;
        firstScreen = new AnimationScreen(this, animation);
    }

    @Override
    public void create() {
        skin = InternalLoader.INSTANCE.loadSkin("skin/glassy-ui.json");
        bitmapFont = InternalLoader.INSTANCE.loadFont();
        fontShader = InternalLoader.INSTANCE.loadFontShader();
        batch = new SpriteBatch();
        shapeDrawer = new ShapeDrawer(batch, InternalLoader.INSTANCE.whitePixel());
        multiplexer = new InputMultiplexer();

        if (!fontShader.isCompiled()) {
            Gdx.app.error("fontShader", "compilation failed: " + fontShader.getLog());
        }

        setScreen(firstScreen);
    }

    @Override
    public void dispose() {
        animationLoader.exit();
        batch.dispose();
        animationLoader.save();
    }

    public static final int DISPLAY_WIDTH = 1920;
    public static final int DISPLAY_HEIGHT = 1080;
}
