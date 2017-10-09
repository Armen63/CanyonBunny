package com.armen;

import com.armen.game.Assets;
import com.armen.screens.DirectedGame;
import com.armen.screens.MenuScreen;
import com.armen.screens.transition.ScreenTransition;
import com.armen.screens.transition.ScreenTransitionSlice;
import com.armen.util.AudioManager;
import com.armen.util.GamePreferences;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Interpolation;


/**
 * Created by Armen on 9/13/2017.
 */

public class CanyonBunnyMain extends DirectedGame {

    private static final String LOG_TAG = CanyonBunnyMain.class.getName();

    public CanyonBunnyMain() {
    }

    @Override
    public void create() {

        // Set LibGdx log level
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        // Load assets
        Assets.instance.init(new AssetManager());

        // Load preferences for audio settings and start playing music
        GamePreferences.instance.load();
        AudioManager.instance.play(Assets.instance.music.song01);

        // Start game at menu screen
        ScreenTransition transition = ScreenTransitionSlice.init(2, ScreenTransitionSlice.UP_DOWN, 10, Interpolation.pow5Out);
        setScreen(new MenuScreen(this), transition);
    }

    @Override
    public void dispose() {
        Assets.instance.dispose();
    }
}
