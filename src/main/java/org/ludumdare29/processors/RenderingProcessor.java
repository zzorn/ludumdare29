package org.ludumdare29.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.time.Time;
import org.ludumdare29.SkyAttribute;
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

    private final Shader shader;

    private final Matrix4 temp = new Matrix4();
    private ModelInstance skySphere;

    private final Renderable tempRenderable = new Renderable();

    public RenderingProcessor(Shader shader) {
        super(RenderingProcessor.class, AppearanceComponent.class, LocationComponent.class);
        this.shader = shader;
    }

    @Override protected void onInit() {
        // Setup model batching
        modelBatch = new ModelBatch();

        // Setup camera
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 1000f;
        cam.update();

        // Setup shader
        shader.init();

        // Setup lighting
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.2f, 0.3f, 1f));
        environment.add(new DirectionalLight().set(0.5f, 0.8f, 0.9f, 0.1f, -1f, 0.1f));

        // Create skydome
        ModelBuilder modelBuilder = new ModelBuilder();
        float skySize = 1000f;
        final Model skySphereModel = modelBuilder.createSphere(-skySize,
                                                          -skySize,
                                                          -skySize,
                                                          128,
                                                          128,
                                                          new Material( SkyAttribute.sky(), ColorAttribute.createDiffuse(Color.GRAY)),
                                                          VertexAttributes.Usage.Position |
                                                          VertexAttributes.Usage.Normal |
                                                          VertexAttributes.Usage.TextureCoordinates);
        skySphere = new ModelInstance(skySphereModel);


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

        // Move sky to center on camera
        skySphere.transform.setTranslation(cam.position);

        // Render sky
        modelBatch.render(skySphere, shader);
    }

    @Override protected void processEntity(Time time, Entity entity) {
        final LocationComponent location = entity.getComponent(LocationComponent.class);
        final AppearanceComponent appearance = entity.getComponent(AppearanceComponent.class);

        if (appearance != null && location != null) {
            final ModelInstance modelInstance = appearance.getAppearance();

            // Apply direction
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
            modelBatch.render(modelInstance, shader);
//            modelBatch.render(modelInstance, environment);
        }
    }

    @Override protected void postProcess(Time time) {
        modelBatch.end();
    }

    @Override public void shutdown() {
        modelBatch.dispose();
        shader.dispose();
    }
}
