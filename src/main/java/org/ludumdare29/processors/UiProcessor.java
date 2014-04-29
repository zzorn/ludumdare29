package org.ludumdare29.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.time.Time;
import org.ludumdare29.components.UiComponent;
import org.ludumdare29.components.appearance.AppearanceComponent;

import java.util.List;

import static org.flowutils.MathUtils.*;

/**
 * Renders the UI
 */
public class UiProcessor extends BaseEntityProcessor {

    private static final Color AMBIENT_LIGHT = new Color(0.1f, 0.2f, 0.3f, 1f);
    private static final Color CABIN_LIGHT1_COLOR = new Color(0.6f, 0.35f, 0.3f, 1f);
    private static final Color CABIN_LIGHT2_COLOR = new Color(0.05f, 0.2f, 0.6f, 1f);
    private static final Color CABIN_LIGHT3_COLOR = new Color(0.6f, 0.4f, 0.3f, 1f);
    private static final float CABIN_LIGHT1_INTENSITY = 8f;
    private static final float CABIN_LIGHT2_INTENSITY = 10f;
    private static final float CABIN_LIGHT3_INTENSITY = 8f;
    private static final Vector3 CABIN_LIGHT1_POS = new Vector3(-1f, 1f, -0.3f);
    private static final Vector3 CABIN_LIGHT2_POS = new Vector3(0.7f, -1.5f, -1.2f);
    private static final Vector3 CABIN_LIGHT3_POS = new Vector3(1.1f, 0.6f, -0.3f);

    private static final Color DIRECTIONAL_LIGHT_COLOR = new Color(0.7f, 0.6f, 0.4f, 1f);
    private static final Vector3 DIRECTIONAL_LIGHT_DIRECTION = new Vector3(0.1f, -1f, 0.3f).nor();

    public PerspectiveCamera camera;
    public ModelBatch modelBatch;
    public Environment environment;

    public boolean uiVisible = true;

    private final Quaternion tempQ = new Quaternion();

    private final Vector3 upperLeftCorner = new Vector3();
    private final Vector3 lowerRightCorner = new Vector3();

    public UiProcessor() {
        super(UiProcessor.class, UiComponent.class, AppearanceComponent.class);
    }

    public boolean isUiVisible() {
        return uiVisible;
    }

    public void setUiVisible(boolean uiVisible) {
        this.uiVisible = uiVisible;
    }

    @Override protected void onInit() {
        // Setup model batching
        modelBatch = new ModelBatch();

        // Setup camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 0f, 0f);
        camera.lookAt(0,0,-1);
        camera.near = 0.01f;
        camera.far = 100f;
        camera.update();

        // Setup lighting
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, AMBIENT_LIGHT));
        //environment.add(new DirectionalLight().set(DIRECTIONAL_LIGHT_COLOR, DIRECTIONAL_LIGHT_DIRECTION));
        environment.add(new PointLight().set(CABIN_LIGHT1_COLOR, CABIN_LIGHT1_POS, CABIN_LIGHT1_INTENSITY));
        environment.add(new PointLight().set(CABIN_LIGHT2_COLOR, CABIN_LIGHT2_POS, CABIN_LIGHT2_INTENSITY));
        environment.add(new PointLight().set(CABIN_LIGHT3_COLOR, CABIN_LIGHT3_POS, CABIN_LIGHT3_INTENSITY));
    }

    @Override protected void preProcess(Time time) {
        // Clear depth
        //Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        // Render scene
        modelBatch.begin(camera);
    }


    @Override protected void processEntity(Time time, Entity entity) {
        final UiComponent ui = entity.getComponent(UiComponent.class);
        final AppearanceComponent appearance = entity.getComponent(AppearanceComponent.class);

        if (uiVisible && appearance != null && ui != null && appearance.isVisible()) {
            // Do any appearance updates
            appearance.update(time);

            // Render all parts of the appearance
            final List<ModelInstance> modelInstances = appearance.getModelInstances();
            for (ModelInstance modelInstance : modelInstances) {
                // Calculate UI extent
                float distanceToUiAlongEdges = 2;
                camera.getPickRay(0, 0).getEndPoint(upperLeftCorner, distanceToUiAlongEdges);
                camera.getPickRay(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()).getEndPoint(lowerRightCorner, distanceToUiAlongEdges);

                // Calculate position
                float x = mix(ui.relativeX, upperLeftCorner.x, lowerRightCorner.x);
                float y = mix(ui.relativeY, upperLeftCorner.y, lowerRightCorner.y);
                float z = mix(0.5f, upperLeftCorner.z, lowerRightCorner.z);

                //modelInstance.calculateTransforms();
                modelInstance.transform.idt();
                modelInstance.transform.setTranslation(x, y, z);
                modelInstance.transform.translate(appearance.getOffset());

                // Apply scaling
                modelInstance.transform.scl(appearance.getScale());

                // Rotate to face camera
                modelInstance.transform.rotate(1, 0, 0, 90);

                // Render
                modelBatch.render(modelInstance, environment);
            }
        }

    }

    @Override protected void postProcess(Time time) {
        modelBatch.end();
    }

    @Override public void shutdown() {
        modelBatch.dispose();
    }

}
