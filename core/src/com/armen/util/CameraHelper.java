package com.armen.util;

/**
 * Created by Armen on 9/16/2017.
 */

import com.armen.game.objects.AbstractGameObject;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import static com.armen.util.Constants.MAX_ZOOM_IN;
import static com.armen.util.Constants.MAX_ZOOM_OUT;

public class CameraHelper {
    private static final String LOG_TAG = CameraHelper.class.getName();

    private final float FOLLOW_SPEED = 4.0f;
    private Vector2 position;
    private float zoom;

    private AbstractGameObject L;


    private AbstractGameObject target;

    public CameraHelper() {
        position = new Vector2();
        zoom = 1.0f;
    }

    public void update(float deltaTime) {
        if (!hasTarget()) return;

        position.lerp(target.position, FOLLOW_SPEED * deltaTime);

//        position.x = target.position.x + target.origin.x;
//        position.y = target.position.y + target.origin.y;

        // Prevent camera from moving down too far
        position.y = Math.max(-1f, position.y);
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void addZoom(float amount) {
        setZoom(zoom + amount);
    }

    public void setZoom(float zoom) {
        this.zoom = MathUtils.clamp(zoom, MAX_ZOOM_IN, MAX_ZOOM_OUT);
    }

    public void setTarget(AbstractGameObject target) {
        this.target = target;
    }

    public boolean hasTarget(AbstractGameObject target) {
        return hasTarget() && this.target.equals(target);
    }

    public float getZoom() {
        return zoom;
    }

    public boolean hasTarget() {
        return target != null;
    }

    public void applyTo(OrthographicCamera camera) {
        camera.position.x = position.x;
        camera.position.y = position.y;
        camera.zoom = zoom;
        camera.update();
    }
}