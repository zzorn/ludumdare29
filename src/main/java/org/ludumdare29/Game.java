package org.ludumdare29;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.world.ConcurrentWorld;
import org.flowutils.time.RealTime;
import org.ludumdare29.processors.*;
import org.ludumdare29.shader.OceanShader;

import java.util.Random;

/**
 * Main entrypoint.
 */
public class Game extends ApplicationAdapter {

    public static final String NAME = "Crushing Depth";
    public static final int SIMULATION_STEP_MILLISECONDS = 5;
    private ConcurrentWorld world;
    private RealTime time;
    private Sea sea;
    private BubbleProcessor bubbleProcessor;
    private PhysicsProcessor physicsProcessor;
    private RenderingProcessor renderingProcessor;
    private Vector3 tempPos;
    private EntityFactory entityFactory;
    private ShipProcessor shipProcessor;
    private final InputMultiplexer inputHandler = new InputMultiplexer();

    public static void main(String[] args) {
        Game game = new Game();
        game.start();

        /* Ascii table for font
        for (int i = 32; i < 256; i++) {
            if ((i % 16) == 0) System.out.println();
            System.out.print((char) (i));
        }
        */
    }


    private void start() {
        // Create world
        time = new RealTime();
        world = new ConcurrentWorld(time, SIMULATION_STEP_MILLISECONDS);
        sea = new Sea();

        entityFactory = new EntityFactory(world, sea);

        // Add processors
        world.addProcessor(new TrackingProcessor());
        world.addProcessor(new EnemyProcessor(entityFactory));
        world.addProcessor(new BubblingProcessor(entityFactory));
        world.addProcessor(new TorpedoTubeProcessor(entityFactory));
        bubbleProcessor = world.addProcessor(new BubbleProcessor(sea));
        physicsProcessor = world.addProcessor(new PhysicsProcessor(sea));
        shipProcessor = world.addProcessor(new ShipProcessor());
        world.addProcessor(new SubmarineProcessor(sea));
        world.addProcessor(new RocketProcessor());
        world.addProcessor(new ExplodingProcessor(entityFactory));
        world.addProcessor(new DamageProcessor(entityFactory));
        renderingProcessor = new RenderingProcessor(new OceanShader(sea));
        final UiProcessor uiProcessor = new UiProcessor();
        world.addProcessor(new CameraProcessor(renderingProcessor, inputHandler, uiProcessor));
        world.addProcessor(renderingProcessor);
        world.addProcessor(uiProcessor);

        // Create 3D application
        new LwjglApplication(this, NAME, 1024, 768);
    }


    @Override public void create() {
        // Setup input handler
        Gdx.input.setInputProcessor(inputHandler);

        // Initialize processors
        world.init();

        tempPos = new Vector3();

        // Create player submarine
        final Entity player = entityFactory.createPlayerSubmarine(tempPos.set(0, 0, 0), 0.3f, 0.7f, inputHandler);

        // Create some bubbles
        Random random = new Random();
        float spread = 2000;
        for (int i = 0; i < 20; i++) {

            final float diam = random.nextFloat() * 0.1f + 0.001f;

            tempPos.set((float) random.nextGaussian() * spread,
                        (float) random.nextGaussian() * spread - 10,
                        (float) random.nextGaussian() * spread);

            entityFactory.createVaryingBubbleCloud(tempPos, 50 + random.nextInt(100), diam, random.nextFloat() * 1, 60f);
        }

        // Create some submarines
        spread = 300;
        for (int i = 0; i < 10; i++) {

            tempPos.set((float) random.nextGaussian() * spread,
                        (float) random.nextGaussian() * spread ,
                        (float) random.nextGaussian() * spread);

            //tempPos.set((i% 10) * 10 - 100, -i * 2 + 100, (i / 10) * 10 -100  );

            entityFactory.createEnemySubmarine(tempPos,
                                          random.nextFloat() * random.nextFloat(),
                                          random.nextFloat() * random.nextFloat());
        }
    }

    @Override public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override public void render() {
        // Tick
        time.nextStep();

        // Update world
        world.process();
    }

}
