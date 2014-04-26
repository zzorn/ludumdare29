package org.ludumdare29;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.persistence.NoPersistence;
import org.entityflow.world.ConcurrentWorld;
import org.flowutils.time.RealTime;
import org.ludumdare29.processors.BubbleProcessor;
import org.ludumdare29.processors.BubblingProcessor;
import org.ludumdare29.processors.PhysicsProcessor;
import org.ludumdare29.processors.RenderingProcessor;

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

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }


    private void start() {
        // Create world
        time = new RealTime();
        world = new ConcurrentWorld(time, new NoPersistence(), SIMULATION_STEP_MILLISECONDS);
        sea = new Sea();

        entityFactory = new EntityFactory(world, sea);

        // Add processors
        world.addProcessor(new BubblingProcessor(entityFactory));
        bubbleProcessor = world.addProcessor(new BubbleProcessor(sea));
        physicsProcessor = world.addProcessor(new PhysicsProcessor(sea));
        renderingProcessor = world.addProcessor(new RenderingProcessor());

        // Create 3D application
        new LwjglApplication(this, NAME, 1000, 800);
    }


    @Override public void create() {
        // Initialize processors
        world.init();

        // Create some bubbles
        Random random = new Random();
        tempPos = new Vector3();
        float spread = 20;
        for (int i = 0; i < 20; i++) {

            final float diam = random.nextFloat() * 0.1f + 0.001f;

            tempPos.set((float) random.nextGaussian() * spread,
                        (float) random.nextGaussian() * spread - 10,
                        (float) random.nextGaussian() * spread);

            entityFactory.createVaryingBubbleCloud(tempPos, 50 + random.nextInt(100), diam, random.nextFloat() * 1, 60f);
        }

        // Create some submarines
        spread = 100;
        for (int i = 0; i < 200; i++) {
            /*
            tempPos.set((float) random.nextGaussian() * spread,
                        (float) random.nextGaussian() * spread ,
                        (float) random.nextGaussian() * spread);
                        */
            tempPos.set((i% 10) * 10 - 100, -i * 2 + 100, (i / 10) * 10 -100  );

            entityFactory.createSubmarine(tempPos);
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
