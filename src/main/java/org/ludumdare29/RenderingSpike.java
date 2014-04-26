package org.ludumdare29;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test rendering and 3D
 */
public class RenderingSpike implements ApplicationListener {

    public static final String NAME = "Crushing Depth";

    public PerspectiveCamera cam;
    public ModelBatch modelBatch;
    public Model model;
    public ModelInstance instance;
    public Environment environment;
    public CameraInputController camController;

    private List<ModelInstance> entries = new ArrayList<>();

    public void create () {
        // Setup model batching
        modelBatch = new ModelBatch();

        // Setup camera
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        // Create test model
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(5f, 5f, 5f,
                                       new Material(ColorAttribute.createDiffuse(Color.BLUE)),
                                       VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // Create instance of model to render
        instance = new ModelInstance(model);
        entries.add(instance);

        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            final ModelInstance cube = new ModelInstance(model);
            cube.transform.translate((float) random.nextGaussian() * 100,
                                     (float) random.nextGaussian() * 100,
                                     (float) random.nextGaussian() * 100);
            cube.transform.rotate((float) random.nextGaussian() * 10,
                                  (float) random.nextGaussian() * 10,
                                  (float) random.nextGaussian() * 10,
                                  (float) random.nextGaussian() * 180);
            entries.add(cube);
        }

        // Setup lighting
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // Setup camera control
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
    }

    public void render () {
        // Update from input
        camController.update();

        // Clear screen
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render scene
        modelBatch.begin(cam);
        modelBatch.render(entries, environment);
        modelBatch.end();
    }

    public void resize (int width, int height) {
    }

    public void pause () {
    }

    public void resume () {
    }

    public void dispose () {
        modelBatch.dispose();
        model.dispose();
    }


    public static void main(String[] args) {

        new LwjglApplication(new RenderingSpike(), NAME, 800, 600);

    }

}


