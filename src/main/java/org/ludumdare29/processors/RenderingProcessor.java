package org.ludumdare29.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Matrix4;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.time.Time;
import org.ludumdare29.components.LocationComponent;
import org.ludumdare29.components.appearance.AppearanceComponent;

/**
 *
 */
public class RenderingProcessor extends BaseEntityProcessor {

    public PerspectiveCamera cam;
    public ModelBatch modelBatch;
    public Environment environment;
    public CameraInputController camController;

    private final Matrix4 temp = new Matrix4();

    public RenderingProcessor() {
        super(RenderingProcessor.class, AppearanceComponent.class, LocationComponent.class);
    }

    @Override protected void onInit() {
        // Setup model batching
        modelBatch = new ModelBatch();

        // Setup camera
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        // Setup lighting
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // Setup camera control
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
    }

    @Override protected void preProcess(Time time) {
        // Update from input
        camController.update();

        // Clear screen
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render scene
        modelBatch.begin(cam);
    }

    @Override protected void processEntity(Time time, Entity entity) {
        final LocationComponent location = entity.getComponent(LocationComponent.class);
        final AppearanceComponent appearance = entity.getComponent(AppearanceComponent.class);

        if (appearance != null && location != null) {
            final ModelInstance modelInstance = appearance.getAppearance();

            modelInstance.transform.idt();
            modelInstance.transform.rotate(location.direction);

            // Update position
            final float x = location.position.x + appearance.getOffset().x;
            final float y = location.position.y + appearance.getOffset().y;
            final float z = location.position.z + appearance.getOffset().z;
            modelInstance.transform.setTranslation(x, y, z);

            // Apply scaling
            modelInstance.transform.scl(appearance.getScale());

            // Render
            modelBatch.render(modelInstance, environment);
        }
    }

    @Override protected void postProcess(Time time) {
        modelBatch.end();
    }

    @Override public void shutdown() {
        modelBatch.dispose();
    }
}
