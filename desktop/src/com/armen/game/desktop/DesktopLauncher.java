package com.armen.game.desktop;

import com.armen.CanyonBunnyMain;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class DesktopLauncher {
    public static void main (String[] arg) {
        boolean rebuildAtlas = true;

        if (rebuildAtlas) {
            TexturePacker.Settings settings = new TexturePacker.Settings();
            settings.maxWidth = 1024;
            settings.maxHeight = 1024;
            settings.duplicatePadding = false;
            settings.debug = false;

            TexturePacker.process(settings, "../../desktop/assets-raw/images", "../../android/assets/images", "canyonbunny.atlas");
            TexturePacker.process(settings, "../../desktop/assets-raw/images-ui", "../../android/assets/images", "canyonbunny-ui.atlas");

        }
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        new LwjglApplication(new CanyonBunnyMain(), config);
    }
}
