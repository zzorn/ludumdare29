package org.ludumdare29;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.world.World;
import org.flowutils.MathUtils;
import org.ludumdare29.components.*;
import org.ludumdare29.components.appearance.BubbleAppearance;
import org.ludumdare29.components.appearance.SubmarineAppearance;
import org.ludumdare29.processors.BubbleProcessor;

import java.util.Random;

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

    public Entity createSubmarine(Vector3 pos, float sizeFactor) {
        LocationComponent location = new LocationComponent(pos);
        location.direction.setFromAxisRad(0, 1, 0, random.nextFloat() * MathUtils.TauFloat);
        SubmarineAppearance appearance = new SubmarineAppearance(MathUtils.mixAndClamp(sizeFactor, 5f, 100f),
                                                                 MathUtils.mixAndClamp(sizeFactor, 3f, 16f));
        PhysicalComponent physical = new PhysicalComponent(10000f, random.nextFloat() * 200f + 900f, 0.1f);
        BubblingComponent bubbling = new BubblingComponent(10, 30, 0.3f, appearance.width, 10, true, true, true);
        bubbling.bubblingPosOffset.set(appearance.getPropellerOffset());
        SubmarineComponent submarine = new SubmarineComponent();
        ShipComponent ship = new ShipComponent();
        return world.createEntity(location, appearance, bubbling, physical, ship, submarine);
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
            float size = MathUtils.mix(relPos * relPos * relPos, averageBubbleDiam_m * 0.25f, averageBubbleDiam_m * 4f);
            float lifeTime = MathUtils.mix(relPos * relPos, lifeTime_seconds * 0.5f, lifeTime_seconds * 1.5f);

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
