package org.ludumdare29.components;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import org.entityflow.component.BaseComponent;

/**
 * Can launch torpedoes.
 */
public class TorpedoTubeComponent extends BaseComponent {

    public float reloadTime_s = 5;
    public float torpedoSizeFactor = 0.5f;
    public float torpedoSpeedFactor = 0.5f;
    public float secondsUntilReloaded = 0;
    public int launchKeyCode = Input.Keys.SPACE;
    public boolean launchRequested = false;

    public final InputAdapter inputHandler = new InputAdapter() {
        @Override public boolean keyDown(int keycode) {
            if (keycode == launchKeyCode) {
                requestLaunch();
            }
            return false;
        }
    };

    public TorpedoTubeComponent(float reloadTime_s, float torpedoSizeFactor, float torpedoSpeedFactor) {
        this.reloadTime_s = reloadTime_s;
        this.torpedoSizeFactor = torpedoSizeFactor;
        this.torpedoSpeedFactor = torpedoSpeedFactor;
    }

    public boolean isReadyToFire() {
        return secondsUntilReloaded <= 0;
    }

    public void requestLaunch() {
        if (isReadyToFire()) {
            launchRequested = true;
        }
    }

    public boolean isLaunchRequested() {
        return launchRequested;
    }

    public InputAdapter getInputHandler() {
        return inputHandler;
    }
}
