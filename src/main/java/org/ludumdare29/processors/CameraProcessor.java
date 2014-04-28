package org.ludumdare29.processors;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.MathUtils;
import org.flowutils.time.Time;
import org.ludumdare29.components.CameraComponent;
import org.ludumdare29.components.LocationComponent;
import org.ludumdare29.components.appearance.AppearanceComponent;

/**
 * Manages the cameras available in the world for the player
 */
public class CameraProcessor extends BaseEntityProcessor {

    private final RenderingProcessor renderingProcessor;

    private Entity currentCameraEntity = null;

    private static final Vector3 UP_VECTOR = new Vector3(0, 1, 0);
    private final Matrix4 tempM = new Matrix4();
    private final Vector3 tempV = new Vector3();
    private final Quaternion tempQ = new Quaternion();

    private final InputAdapter cameraInputHandler = new InputAdapter() {
        @Override public boolean keyUp(int keycode) {
            // Switch to next camera on enter
            if (keycode == Input.Keys.ENTER) {
                Entity nextCameraEntity = getNextEntity(CameraProcessor.this.currentCameraEntity);
                setCurrentCameraEntity(nextCameraEntity);
            }

            return false;
        }
    };

    public CameraProcessor(RenderingProcessor renderingProcessor, InputMultiplexer inputHandler) {
        super(CameraProcessor.class, LocationComponent.class, CameraComponent.class);
        this.renderingProcessor = renderingProcessor;

        inputHandler.addProcessor(cameraInputHandler);
    }

    @Override protected void handleAddedEntity(Entity entity) {
        if (currentCameraEntity == null) setCurrentCameraEntity(entity);
    }

    @Override protected void handleRemovedEntity(Entity entity) {
        if (entity == currentCameraEntity) setCurrentCameraEntity(null);
    }

    @Override protected void processEntity(Time time, Entity entity) {
        if (currentCameraEntity == null) setCurrentCameraEntity(entity);
    }

    @Override protected void postProcess(Time time) {
        renderingProcessor.setCameraHostEntity(currentCameraEntity);

        if (currentCameraEntity != null) {
            final CameraComponent camera = currentCameraEntity.getComponent(CameraComponent.class);

            updateCameraPos();
        }

    }

    /**
     * @return input handler used to switch the camera etc.
     */
    public final InputProcessor getCameraInputHandler() {
        return cameraInputHandler;
    }

    private void setCurrentCameraEntity(Entity entity) {
        if (currentCameraEntity != entity) {
            setVisibilityOfAssociatedEntityToHide(currentCameraEntity, true);
            currentCameraEntity = entity;
            setVisibilityOfAssociatedEntityToHide(currentCameraEntity, false);
        }
    }

    private void setVisibilityOfAssociatedEntityToHide(final Entity cameraEntity, final boolean visible) {
        if (cameraEntity != null) {
            final CameraComponent camera = cameraEntity.getComponent(CameraComponent.class);
            if (camera.entityToHideWhenCameraActive != null) {
                final AppearanceComponent appearance = camera.entityToHideWhenCameraActive.getComponent(AppearanceComponent.class);
                appearance.setVisible(visible);
            }
        }
    }

    private void updateCameraPos() {

        if (currentCameraEntity != null) {
            final LocationComponent location = currentCameraEntity.getComponent(LocationComponent.class);
            final CameraComponent cameraComponent = currentCameraEntity.getComponent(CameraComponent.class);
            if (location != null) {
                final PerspectiveCamera camera = getCamera();

                // Set camera position to middle of sub
                camera.position.set(location.position);

                // Look in direction of sub
                tempV.set(-1, 0, 0);
                location.direction.transform(tempV);
                camera.direction.set(tempV);

                if (cameraComponent.keepUpright) {
                    // Keep camera up as world up
                    camera.up.set(UP_VECTOR);
                } else {
                    // Keep camera up aligned to the sub
                    tempV.set(UP_VECTOR);
                    location.direction.transform(tempV);
                    camera.up.set(tempV);
                }

                // Set fov
                setFieldOfView_degrees(cameraComponent.fieldOfView_degrees);

                // Update camera projection
                camera.update();
            }
        }
    }

    private void setFieldOfView_degrees(float degrees) {
        final PerspectiveCamera camera = getCamera();

        float fov = MathUtils.clamp(degrees, 0.001f, 360);
        camera.fieldOfView = fov;
        camera.update();
    }

    private PerspectiveCamera getCamera() {
        return renderingProcessor.getCamera();
    }



    private Entity getNextEntity(final Entity currentEntity) {
        // Select the next camera from the current camera, or the first one if the current camera was last
        Entity nextEntity = null;
        for (Entity entity : getHandledEntities()) {
            if (nextEntity == null) nextEntity = entity;
            if (entity == currentEntity) nextEntity = null;
        }
        return nextEntity;
    }
}
