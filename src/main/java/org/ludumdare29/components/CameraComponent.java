package org.ludumdare29.components;

import org.entityflow.component.BaseComponent;
import org.entityflow.entity.Entity;
import org.flowutils.MathUtils;

/**
 *
 */
public class CameraComponent extends BaseComponent {

    public Entity entityToHideWhenCameraActive = null;
    public float fieldOfView_degrees = 67;
    public boolean keepUpright = true;

    public CameraComponent() {
    }

    public CameraComponent(Entity entityToHideWhenCameraActive) {
        this.entityToHideWhenCameraActive = entityToHideWhenCameraActive;
    }

    public CameraComponent(Entity entityToHideWhenCameraActive, float fieldOfView_degrees) {
        this.entityToHideWhenCameraActive = entityToHideWhenCameraActive;
        this.fieldOfView_degrees = fieldOfView_degrees;
    }

    public CameraComponent(Entity entityToHideWhenCameraActive, float fieldOfView_degrees, boolean keepUpright) {
        this.entityToHideWhenCameraActive = entityToHideWhenCameraActive;
        this.fieldOfView_degrees = fieldOfView_degrees;
        this.keepUpright = keepUpright;
    }
}
