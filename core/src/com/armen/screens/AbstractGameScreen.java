package com.armen.screens;

import com.armen.game.Assets;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;

/**
 * Created by Armen on 9/26/2017.
 */

public abstract class AbstractGameScreen implements Screen {
    protected DirectedGame game;
    public abstract InputProcessor getInputProcessor();


    public AbstractGameScreen(DirectedGame game) {
        this.game = game;
    }

    public abstract void render(float deltaTime);

    public abstract void resize(int width, int height);

    public abstract void show();

    public abstract void hide();

    public abstract void pause();

    public void resume() {
        Assets.instance.init(new AssetManager());
    }

    public void dispose() {
        Assets.instance.dispose();
    }
}
