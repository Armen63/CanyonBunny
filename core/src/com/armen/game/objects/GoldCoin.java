package com.armen.game.objects;

import com.armen.game.Assets;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Armen on 9/25/2017.
 */

public class GoldCoin extends AbstractGameObject {

    private TextureRegion regGoldCoin;
    public boolean collected;

    public GoldCoin() {
        init();
    }

    private void init() {
        dimension.set(0.5f, 0.5f);
        setAnimation(Assets.instance.goldCoin.animGoldCoin);
        stateTime = MathUtils.random(0.0f, 1.0f);
//        regGoldCoin = Assets.instance.goldCoin.goldCoin;

        // Set bounding box for collision detection
        bounds.set(0, 0, dimension.x, dimension.y);
        collected = false;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (collected) return;
        TextureRegion reg = null;
        reg = (TextureRegion) animation.getKeyFrame(stateTime, true);
        batch.draw(reg.getTexture(),
                position.x, position.y,
                origin.x, origin.y,
                dimension.x, dimension.y,
                scale.x, scale.y,
                rotation,
                reg.getRegionX(), reg.getRegionY(),
                reg.getRegionWidth(), reg.getRegionHeight(),
                false, false);
    }


    public int getScore() {
        return 100;
    }
}