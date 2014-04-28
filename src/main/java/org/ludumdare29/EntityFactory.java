package org.ludumdare29;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.world.World;
import org.ludumdare29.components.*;
import org.ludumdare29.components.appearance.AppearanceComponent;
import org.ludumdare29.components.appearance.BubbleAppearance;
import org.ludumdare29.components.appearance.SubmarineAppearance;
import org.ludumdare29.processors.BubbleProcessor;

import java.util.Random;

import static org.flowutils.MathUtils.*;

/**
 * Used for creating various types of entities.
 */
public final class EntityFactory {

    private final World world;
    private final Sea sea;
    private final Random random = new Random();

    public EntityFactory(World world, Sea sea) {
        this.world = world;
        this.sea = sea;
    }

    public Entity createSubmarine(Vector3 pos, float sizeFactor, float sleekness) {
        LocationComponent location = new LocationComponent(pos);
        location.direction.setFromAxisRad(0, 1, 0, random.nextFloat() * TauFloat);

        SubmarineAppearance appearance = new SubmarineAppearance(mixAndClamp(sizeFactor, 5f, 100f),
                                                                 mixAndClamp(sizeFactor, 3f, 16f) * mixAndClamp(sleekness, 1.5f, 0.5f));

        final float mass_kg = mixAndClamp(sizeFactor, 10000f, 100000f);
        final float dragCoefficient  = mixAndClamp(sleekness, 0.5f, 0.03f);
        PhysicalComponent physical = new PhysicalComponent(mass_kg, 1000f, dragCoefficient);

        BubblingComponent bubbling = new BubblingComponent(7, 30, 0.3f, appearance.width*0.5f, 15, true, true, true, false);
        bubbling.bubblingPosOffset.set(appearance.getPropellerOffset());

        SubmarineComponent submarine = new SubmarineComponent();

        ShipComponent ship = new ShipComponent();

        return world.createEntity(location, appearance, bubbling, physical, ship, submarine);
    }

    public Entity createPlayerSubmarine(Vector3 pos, float sizeFactor, float sleekness, InputMultiplexer inputMultiplexer) {
        final Entity playerSubmarine = createSubmarine(pos, sizeFactor, sleekness);

        inputMultiplexer.addProcessor(playerSubmarine.getComponent(ShipComponent.class).getInputHandler());
        inputMultiplexer.addProcessor(playerSubmarine.getComponent(SubmarineComponent.class).getInputHandler());

        // Add a first person view camera
        world.createEntity(new LocationComponent(pos),
                           new CameraComponent(playerSubmarine, 67, false),
                           new TrackingComponent(playerSubmarine, new Vector3(0, 6, 0)));

        // Add bridge view
        final Vector3 hatchOffset = ((SubmarineAppearance) playerSubmarine.getComponent(AppearanceComponent.class)).getHatchOffset();
        world.createEntity(new LocationComponent(pos),
                           new CameraComponent(null, 70, true),
                           new TrackingComponent(playerSubmarine, hatchOffset.cpy().add(-1, 0, 0)));

        // Add rear view camera
        world.createEntity(new LocationComponent(pos),
                           new CameraComponent(null, 90, true),
                           new TrackingComponent(playerSubmarine, new Vector3(80, 30, 0)));

        // Add a bubble cloud around the player to help visually orient them
        world.createEntity(new LocationComponent(pos),
                           new BubblingComponent(0.5f, 20, 0.05f, 200, 6, true, false, true, false),
                           new TrackingComponent(playerSubmarine, new Vector3(0, -20, 0)));

        return playerSubmarine;
    }


    /**
     * Creates a cloud of bubbles of varying sizes around the specified location.
     *
     * @param pos center of cloud
     * @param numBubbles approximate number of bubbles in the cloud
     * @param averageBubbleDiam_m average diameter of each bubble. Also some larger bubbles are added.
     * @param bubbleCloudDiam_m maximum size of the bubble cloud along each coordinate axis.
     * @param lifeTime_seconds average lifetime for the bubbles, before they dissappear.
     */
    public void createVaryingBubbleCloud(Vector3 pos,
                                         int numBubbles,
                                         float averageBubbleDiam_m,
                                         float bubbleCloudDiam_m,
                                         float lifeTime_seconds) {

        Vector3 bubblePos = new Vector3();

        for (int i = 0; i < numBubbles; i++) {
            float relPos = (i + 1f) / numBubbles;
            float size = mix(relPos * relPos * relPos, averageBubbleDiam_m * 0.25f, averageBubbleDiam_m * 4f);
            float lifeTime = mix(relPos * relPos, lifeTime_seconds * 0.5f, lifeTime_seconds * 1.5f);

            bubblePos.set(pos);
            bubblePos.add(randomNormalDistributed(bubbleCloudDiam_m),
                          randomNormalDistributed(bubbleCloudDiam_m),
                          randomNormalDistributed(bubbleCloudDiam_m));

            final float bubbleDiam_m = 0.001f + randomPositiveNormalDistributed(size * 2);

            createBubble(bubblePos, bubbleDiam_m, lifeTime);
        }


    }

    /**
     * Creates a cloud of bubbles around the specified location.
     *
     * @param pos center of cloud
     * @param numBubbles number of bubbles in the cloud
     * @param averageBubbleDiam_m average diameter of each bubble.
     * @param bubbleCloudDiam_m maximum size of the bubble cloud along each coordinate axis.
     */
    public void createBubbleCloud(Vector3 pos, int numBubbles, float averageBubbleDiam_m, float bubbleCloudDiam_m, float lifeTime_seconds) {
        Vector3 bubblePos = new Vector3();

        for (int i = 0; i < numBubbles; i++) {
            bubblePos.set(pos);
            bubblePos.add(randomNormalDistributed(bubbleCloudDiam_m),
                          randomNormalDistributed(bubbleCloudDiam_m),
                          randomNormalDistributed(bubbleCloudDiam_m));

            final float bubbleDiam_m = 0.01f + randomPositiveNormalDistributed(averageBubbleDiam_m * 2);

            float lifeTime = lifeTime_seconds * 0.5f + randomNormalDistributed(lifeTime_seconds * 0.5f);

            createBubble(bubblePos, bubbleDiam_m, lifeTime);
        }
    }

    public Entity createBubble(Vector3 pos, float diam_m, float lifeTime_seconds) {
        if (diam_m > 0 && lifeTime_seconds > 0 && sea.getDepth(pos) > BubbleProcessor.BUBBLE_POP_DEPTH) {
            final float radius = diam_m * 0.5f;
            final Color color = new Color(0.6f, 0.6f, 0.6f, 0.5f);
            final BubbleAppearance appearance = new BubbleAppearance(color);
            final LocationComponent location = new LocationComponent(pos);
            final PhysicalComponent physical = PhysicalComponent.fromRadiusAndDensity(radius, Sea.AIR_DENSITY_AT_SEA_LEVEL);
            final BubbleComponent bubble = new BubbleComponent(lifeTime_seconds);

            return world.createEntity(appearance, location, bubble, physical);

        }
        else {
            return null;
        }
    }

    private float randomNormalDistributed(float scale) {
        return (2f*random.nextFloat() - 1f) *
               (2f*random.nextFloat() - 1f) *
               scale * 0.5f;
    }

    private float randomPositiveNormalDistributed(float scale) {
        return 0.5f * scale + randomNormalDistributed(scale);
    }

}
