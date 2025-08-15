package com.wamteavm;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.wamteavm.loaders.InternalLoader;
import com.wamteavm.loaders.externalloaders.APIExternalLoader;
import com.wamteavm.loaders.externalloaders.AbstractExternalLoader;
import com.wamteavm.models.Animation;
import com.wamteavm.screens.AnimationScreen;
import com.wamteavm.screens.LoadingScreen;
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
    public AbstractExternalLoader loader;
    private final Screen firstScreen;

    public WarAnimator(AbstractExternalLoader loader) {
        this.loader = loader;
        firstScreen = new MenuScreen(this);
    }

    public WarAnimator(AuthResult authResult, Animation animation) { // Skip login, go directly to AnimationScreen. Only from direct edit animation link
        this.loader = APIExternalLoader.INSTANCE;
        APIExternalLoader.INSTANCE.getApi().setAuthToken(authResult.getToken());
        loader = APIExternalLoader.INSTANCE;
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

        setScreen(new LoadingScreen(this));

        loader.loadAnimations(() -> {setScreen(firstScreen); return null;});
    }

    @Override
    public void dispose() {
        loader.exit();
        batch.dispose();
        loader.saveAnimations();
    }

    public static final int DISPLAY_WIDTH = 1920;
    public static final int DISPLAY_HEIGHT = 1080;
}
