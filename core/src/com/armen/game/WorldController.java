package com.armen.game;

import com.armen.game.objects.BunnyHead;
import com.armen.game.objects.Feather;
import com.armen.game.objects.GoldCoin;
import com.armen.game.objects.Rock;
import com.armen.screens.DirectedGame;
import com.armen.screens.MenuScreen;
import com.armen.screens.transition.ScreenTransition;
import com.armen.screens.transition.ScreenTransitionSlide;
import com.armen.util.AudioManager;
import com.armen.util.CameraHelper;
import com.armen.util.Constants;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;


/**
 * Created by Armen on 9/13/2017.
 */

public class WorldController extends InputAdapter {
    public static final String LOG_TAG = WorldController.class.getName();

    private DirectedGame game;
    public Level level;
    public int lives;
    public int score;
    public CameraHelper cameraHelper;
    private float timeLeftGameOverDelay;
    public float livesVisual;
    public float scoreVisual;

    private boolean accelerometerAvailable;


    private Rectangle r1 = new Rectangle();
    private Rectangle r2 = new Rectangle();

    public WorldController(DirectedGame game) {
        this.game = game;
        init();
    }

    private void init() {
        accelerometerAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
        cameraHelper = new CameraHelper();
        lives = Constants.LIVES_START;
        livesVisual = lives;
        timeLeftGameOverDelay = 0;
        initLevel();
    }

    private void initLevel() {
        score = 0;
        scoreVisual = score;
        level = new Level(Constants.LEVEL_01);
        cameraHelper.setTarget(level.bunnyHead);
    }

    @Override
    public boolean keyUp(int keycode) {
        // Reset game world
        if (keycode == Keys.R) {
            init();
            Gdx.app.debug(LOG_TAG, "Game world resetted");
        }
        //Toggle camera follow
        else if (keycode == Keys.ENTER) {
            cameraHelper.setTarget(cameraHelper.hasTarget() ? null : level.bunnyHead);
            Gdx.app.debug(LOG_TAG, "Camera follow enabled: " + cameraHelper.hasTarget());
        }

        //Back to Menu
        else if (keycode == Keys.ESCAPE || keycode == Keys.BACK) {
            backToMenu();
        }
        return false;
    }


    private Pixmap createProceduralPixmap(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        // Fill square with red color at 50% opacity
        pixmap.setColor(1, 0, 0, 0.5f);
        pixmap.fill();

        // Draw a yellow-colored X shape on square
        pixmap.setColor(1, 1, 0, 1);
        pixmap.drawLine(0, 0, width, height);
        pixmap.drawLine(width, 0, 0, height);

        // Draw a cyan-colored border around square
        pixmap.setColor(0, 1, 1, 1);
        pixmap.drawRectangle(0, 0, width, height);
        return pixmap;
    }

    public void update(float deltaTime) {
        handleDebugInput(deltaTime);
        if (isGameOver()) {
            timeLeftGameOverDelay -= deltaTime;
            if (timeLeftGameOverDelay < 0) backToMenu();
        } else {
            handleInputGame(deltaTime);
        }

        level.update(deltaTime);
        testCollisions();
        cameraHelper.update(deltaTime);
        if (!isGameOver() && isPlayerInWater()) {
            AudioManager.instance.play(Assets.instance.sounds.liveLost);
            lives--;
            if (isGameOver())
                timeLeftGameOverDelay = Constants.TIME_DELAY_GAME_OVER;
            else
                initLevel();
        }
        level.mountains.updateScrollPosition(cameraHelper.getPosition());
        if (livesVisual > lives)
            //?????
            livesVisual = Math.max(lives, livesVisual - 1 * deltaTime);
        if (scoreVisual < score)
            scoreVisual = Math.min(score, scoreVisual + 250 * deltaTime);
    }

    private void handleDebugInput(float deltaTime) {
        if (Gdx.app.getType() != Application.ApplicationType.Desktop)
            return;
        if (!cameraHelper.hasTarget(level.bunnyHead)) {
            // Camera Controls (move)
            float camMoveSpeed = 5 * deltaTime;
            float camMoveSpeedAccelerationFactor = 5;
            if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) camMoveSpeed *=
                    camMoveSpeedAccelerationFactor;
            if (Gdx.input.isKeyPressed(Keys.RIGHT)) moveCamera(-camMoveSpeed,
                    0);
            if (Gdx.input.isKeyPressed(Keys.LEFT)) moveCamera(camMoveSpeed,
                    0);
            if (Gdx.input.isKeyPressed(Keys.UP)) moveCamera(0, camMoveSpeed);
            if (Gdx.input.isKeyPressed(Keys.DOWN)) moveCamera(0,
                    -camMoveSpeed);
            if (Gdx.input.isKeyPressed(Keys.BACKSPACE))
                cameraHelper.setPosition(0, 0);

            // Camera Controls (zoom)
            float camZoomSpeed = 1 * deltaTime;
            float camZoomSpeedAccelerationFactor = 5;
            if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) camZoomSpeed *=
                    camZoomSpeedAccelerationFactor;
            if (Gdx.input.isKeyPressed(Keys.COMMA))
                cameraHelper.addZoom(camZoomSpeed);
            if (Gdx.input.isKeyPressed(Keys.PERIOD)) cameraHelper.addZoom(
                    -camZoomSpeed);
            if (Gdx.input.isKeyPressed(Keys.SLASH)) cameraHelper.setZoom(1);
        }
    }

    private void handleInputGame(float deltaTime) {
        if (cameraHelper.hasTarget(level.bunnyHead)) {
            // Player Movement
            if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
                level.bunnyHead.velocity.x = -level.bunnyHead.terminalVelocity.x;
            } else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
                level.bunnyHead.velocity.x =
                        level.bunnyHead.terminalVelocity.x;
            } else {
                // Use accelerometer for movement if available
                if (accelerometerAvailable) {
                    // normalize accelerometer values from [-10, 10] to [-1, 1]
                    // which translate to rotations of [-90, 90] degrees
                    float amount = Gdx.input.getAccelerometerY() / 10.0f;
                    amount *= 90.0f;
                    // is angle of rotation inside dead zone?
                    if (Math.abs(amount) < Constants.ACCEL_ANGLE_DEAD_ZONE) {
                        amount = 0;
                    } else {
                        // use the defined max angle of rotation instead of
                        // the full 90 degrees for maximum velocity
                        amount /= Constants.ACCEL_MAX_ANGLE_MAX_MOVEMENT;
                    }
                    level.bunnyHead.velocity.x =
                            level.bunnyHead.terminalVelocity.x * amount;
                }
                // Execute auto-forward movement on non-desktop platform
                else if (Gdx.app.getType() != Application.ApplicationType.Desktop) {
                    level.bunnyHead.velocity.x = level.bunnyHead.terminalVelocity.x;
                }
            }
            // Bunny Jump
            if (Gdx.input.isTouched() ||
                    Gdx.input.isKeyPressed(Keys.SPACE)) {
                level.bunnyHead.setJumping(true);
            } else {
                level.bunnyHead.setJumping(false);
            }
        }
    }

    private void moveCamera(float x, float y) {
        x += cameraHelper.getPosition().x;
        y += cameraHelper.getPosition().y;
        cameraHelper.setPosition(x, y);
    }

    private void onCollisionBunnyHeadWithRock(Rock rock) {
        Gdx.app.log(LOG_TAG, "bunny head with Rock");
        BunnyHead bunnyHead = level.bunnyHead;
        float heightDifference = Math.abs(bunnyHead.position.y
                - (rock.position.y + rock.bounds.height));
        if (heightDifference > 0.25f) {
            boolean hitRightEdge = bunnyHead.position.x > (
                    rock.position.x + rock.bounds.width / 2.0f);
            if (hitRightEdge) {
                bunnyHead.position.x = rock.position.x + rock.bounds.width;
            } else {
                bunnyHead.position.x = rock.position.x -
                        bunnyHead.bounds.width;
            }
            return;
        }
        switch (bunnyHead.jumpState) {
            case GROUNDED:
                break;
            case FALLING:
            case JUMP_FALLING:
                Gdx.app.log(LOG_TAG, "falling");
                bunnyHead.position.y = rock.position.y +
                        bunnyHead.bounds.height + bunnyHead.origin.y;
                bunnyHead.jumpState = BunnyHead.JUMP_STATE.GROUNDED;
                break;
            case JUMP_RISING:
                bunnyHead.position.y = rock.position.y +
                        bunnyHead.bounds.height + bunnyHead.origin.y;
                break;
        }
    }

    private void onCollisionBunnyWithGoldCoin(GoldCoin goldCoin) {
        goldCoin.collected = true;
        AudioManager.instance.play(Assets.instance.sounds.pickupCoin);
        score += goldCoin.getScore();
        Gdx.app.log(LOG_TAG, "Gold coin collected");
    }

    private void onCollisionBunnyWithFeather(Feather feather) {
        feather.collected = true;
        AudioManager.instance.play(Assets.instance.sounds.pickupFeather);
        score += feather.getScore();
        level.bunnyHead.setFeatherPowerUp(true);
        Gdx.app.log(LOG_TAG, "Feather collected");
    }

    private void testCollisions() {
        r1.set(
                level.bunnyHead.position.x, level.bunnyHead.position.y,
                level.bunnyHead.bounds.width, level.bunnyHead.bounds.height
        );

        // Test collision: Bunny Head <-> Rocks
        for (Rock rock : level.rocks) {
            r2.set(
                    rock.position.x, rock.position.y,
                    rock.bounds.width, rock.bounds.height
            );
            if (!r1.overlaps(r2))
                continue;
            onCollisionBunnyHeadWithRock(rock);
            // IMPORTANT: must do all collisions for valid
            // edge testing on rocks.
        }

        // Test collision: Bunny Head <-> Gold Coins
        for (GoldCoin goldCoin : level.goldCoins) {
            if (goldCoin.collected)
                continue;
            r2.set(
                    goldCoin.position.x, goldCoin.position.y,
                    goldCoin.bounds.width, goldCoin.bounds.height
            );
            if (!r1.overlaps(r2))
                continue;
            onCollisionBunnyWithGoldCoin(goldCoin);
            break;
        }
        // Test collision: Bunny Head <-> Feathers
        for (Feather feather : level.feathers) {
            if (feather.collected)
                continue;
            r2.set(feather.position.x, feather.position.y,
                    feather.bounds.width, feather.bounds.height);
            if (!r1.overlaps(r2)) continue;
            onCollisionBunnyWithFeather(feather);
            break;
        }
    }

    private void backToMenu() {
        // switch to menu screen
        ScreenTransition transition = ScreenTransitionSlide.init(0.75f, ScreenTransitionSlide.DOWN, false, Interpolation.bounceOut);
        game.setScreen(new MenuScreen(game), transition);
    }

    public boolean isGameOver() {
        return lives < 0;
    }

    public boolean isPlayerInWater() {
        return level.bunnyHead.position.y < -5;
    }
}
