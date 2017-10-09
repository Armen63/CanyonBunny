package com.armen.game.objects;

import com.armen.game.Assets;
import com.armen.util.AudioManager;
import com.armen.util.CharacterSkin;
import com.armen.util.Constants;
import com.armen.util.GamePreferences;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Armen on 9/25/2017.
 */

public class BunnyHead extends AbstractGameObject {
    public static final String LOG_TAG = BunnyHead.class.getName();

    private final float JUMP_TIME_MAX = 0.3f;
    private final float JUMP_TIME_MIN = 0.1f;
    private final float JUMP_TIME_OFFSET_FLYING = JUMP_TIME_MAX - 0.018f;

    public enum VIEW_DIRECTION {
        LEFT,
        RIGHT
    }

    public enum JUMP_STATE {
        GROUNDED,
        FALLING,
        JUMP_RISING,
        JUMP_FALLING
    }

    public ParticleEffect dustParticles = new ParticleEffect();

    private TextureRegion regHead;
    public VIEW_DIRECTION viewDirection;
    public float timeJumping;
    public JUMP_STATE jumpState;
    public boolean hasFeatherPowerUp;
    public float timeLeftFeatherPowerUp;

    private Animation animNormal;
    private Animation animCopterTransform;
    private Animation animCopterTransformBack;
    private Animation animCopterRotate;

    public BunnyHead() {
        init();
    }

    private void init() {
        dimension.set(1, 1);

        animNormal = Assets.instance.bunny.animNormal;
        animCopterTransform = Assets.instance.bunny.animCopterTransform;
        animCopterTransformBack = Assets.instance.bunny.animCopterTransformBack;
        animCopterRotate = Assets.instance.bunny.animCopterRotate;
        setAnimation(animNormal);

        // Center image on game object
        origin.set(dimension.x / 2, dimension.y / 2);

        // Bounding box for collision detection
        bounds.set(0, 0, dimension.x, dimension.y);

        // Set physics values
        terminalVelocity.set(3.0f, 4.0f);
        friction.set(12.0f, 0.0f);
        acceleration.set(0.0f, -25.0f);

        // View direction
        viewDirection = VIEW_DIRECTION.RIGHT;

        //Jump state
        jumpState = JUMP_STATE.FALLING;
        timeJumping = 0;

        //Power - ups
        hasFeatherPowerUp = false;
        timeLeftFeatherPowerUp = 0;

        //Particles
        dustParticles.load(
                Gdx.files.internal("particles/dust.pfx"),
                Gdx.files.internal("particles")
        );
    }

    public void setJumping(boolean jumpKeyPressed) {
        switch (jumpState) {

            // Character is standing on a platform
            case GROUNDED:
                if (jumpKeyPressed) {
                    AudioManager.instance.play(Assets.instance.sounds.jump);
                    // Start counting jump time from the beginning
                    timeJumping = 0;
                    jumpState = JUMP_STATE.JUMP_RISING;
                }
                break;

            // Rising in the air
            case JUMP_RISING:
                if (!jumpKeyPressed)
                    jumpState = JUMP_STATE.JUMP_FALLING;
                break;

            // Falling down
            case FALLING:

                // Falling down after jump
            case JUMP_FALLING:
                if (jumpKeyPressed && hasFeatherPowerUp) {
                    AudioManager.instance.play(
                            Assets.instance.sounds.jumpWithFeather,
                            1,
                            MathUtils.random(1.0f, 1.1f));

                    timeJumping = JUMP_TIME_OFFSET_FLYING;
                    jumpState = JUMP_STATE.JUMP_RISING;
                }
                break;
        }
    }

    public void setFeatherPowerUp(boolean pickedUp) {
        hasFeatherPowerUp = pickedUp;
        if (pickedUp) {
            timeLeftFeatherPowerUp = Constants.ITEM_FEATHER_POWERUP_DURATION;
        }
    }

    public boolean hasFeatherPowerUp() {
        return hasFeatherPowerUp && timeLeftFeatherPowerUp > 0;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (velocity.x != 0) {
            viewDirection = velocity.x < 0 ? VIEW_DIRECTION.LEFT : VIEW_DIRECTION.RIGHT;
        }
        if (timeLeftFeatherPowerUp > 0) {
            if (animCopterRotate == animCopterTransformBack) {
                // Restart "Transform" animation if another feather power-up
                // was picked up during "TransformBack" animation. Otherwise,
                // the "TransformBack" animation would be stuck while the
                // power-up is still active.
                setAnimation(animCopterTransform);
            }
            timeLeftFeatherPowerUp -= deltaTime;
            if (timeLeftFeatherPowerUp < 0) {
                // disable power-up
                timeLeftFeatherPowerUp = 0;
                setFeatherPowerUp(false);
                setAnimation(animCopterTransformBack);
            }
        }
        dustParticles.update(deltaTime);

        // Change animation state according to feather power-up
        if (hasFeatherPowerUp) {
            if (animation == animNormal) {
                setAnimation(animCopterTransform);
            } else if (animation == animCopterTransform) {
                if (animation.isAnimationFinished(stateTime))
                    setAnimation(animCopterRotate);
            }
        } else {
            if (animation == animCopterRotate) {
                if (animation.isAnimationFinished(stateTime))
                    setAnimation(animCopterTransformBack);
            } else if (animation == animCopterTransformBack) {
                if (animation.isAnimationFinished(stateTime)) {
                    setAnimation(animNormal);
                }
            }
        }
    }

    @Override
    protected void updateMotionY(float deltaTime) {
        switch (jumpState) {
            case GROUNDED:
                Gdx.app.log(LOG_TAG, "bunny head grounded");
                jumpState = JUMP_STATE.FALLING;
                break;
            case JUMP_RISING:
                // Keep track of jump time
                timeJumping += deltaTime;

                //Jump time ledt?
                if (timeJumping <= JUMP_TIME_MAX) {
                    //still jumping
                    velocity.y = terminalVelocity.y;
                }
                break;
            case FALLING:
                break;
            case JUMP_FALLING:
                // Add delta times to track jump time
                timeJumping += deltaTime;

                // Jump to minimal height if jump key was pressed too short
                if (timeJumping > 0 && timeJumping <= JUMP_TIME_MIN) {
                    //Still jumping
                    velocity.y = terminalVelocity.y;
                }
        }
        if (jumpState != JUMP_STATE.GROUNDED) {
            dustParticles.allowCompletion();
            super.updateMotionY(deltaTime);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion reg;

        //Draw Particles
        dustParticles.draw(batch);

        //Apply Skin Color
        batch.setColor(CharacterSkin.values()[GamePreferences.instance.charSkin].getColor());

        float dimCorrectionX = 0;
        float dimCorrectionY = 0;
        if (animation != animNormal) {
            dimCorrectionX = 0.05f;
            dimCorrectionY = 0.2f;
        }

        //Draw image
        reg = (TextureRegion) animation.getKeyFrame(stateTime, true);
        batch.draw(
                reg.getTexture(),
                position.x, position.y,
                origin.x, origin.y,
                dimension.x + dimCorrectionX, dimension.y + dimCorrectionY,
                scale.x, scale.y,
                rotation,
                reg.getRegionX(), reg.getRegionY(),
                reg.getRegionWidth(), reg.getRegionHeight(),
                viewDirection == VIEW_DIRECTION.LEFT, false
        );

        // Reset color to white
        batch.setColor(1, 1, 1, 1);
    }
}