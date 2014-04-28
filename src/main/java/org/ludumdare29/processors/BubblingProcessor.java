package org.ludumdare29.processors;

import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.time.Time;
import org.ludumdare29.EntityFactory;
import org.ludumdare29.components.BubblingComponent;
import org.ludumdare29.components.LocationComponent;

import java.util.Random;

/**
 *
 */
public class BubblingProcessor extends BaseEntityProcessor {

    private final EntityFactory entityFactory;

    public BubblingProcessor(EntityFactory entityFactory) {
        super(BubblingProcessor.class, LocationComponent.class, BubblingComponent.class);

        this.entityFactory = entityFactory;
    }

    private float timeSinceLastCall = 0;
    private final Random random = new Random();
    private final Vector3 pos = new Vector3();

    @Override protected void preProcess(Time time) {
        timeSinceLastCall = (float) time.getSecondsSinceLastStep();
    }

    @Override protected void processEntity(Time time, Entity entity) {
        final LocationComponent location = entity.getComponent(LocationComponent.class);
        final BubblingComponent bubbling = entity.getComponent(BubblingComponent.class);

        // Count down to the next bubbling
        bubbling.secondsUntilNextBubbles -= timeSinceLastCall;

        // Check if we bubble
        if (bubbling.secondsUntilNextBubbles <= 0) {
            // Determine number of bubbles
            final int numBubbles;
            if (bubbling.varyingBubbleCount) {
                numBubbles = random.nextInt(bubbling.bubbleCount);
            }
            else {
                numBubbles = bubbling.bubbleCount;
            }

            // Determine location relative to object to bubble at
            pos.set(bubbling.bubblingPosOffset);
            location.direction.transform(pos);
            pos.add(location.position);

            // Cluster or spread out bubbles
            float cloudDiam = bubbling.bubbleCloudDiam;
            if (bubbling.clusterBubbles) {
                pos.add((float) random.nextGaussian() * bubbling.bubbleCloudDiam,
                        (float) random.nextGaussian() * bubbling.bubbleCloudDiam,
                        (float) random.nextGaussian() * bubbling.bubbleCloudDiam);
                cloudDiam *= 0.01f;
            }

            // Create bubbles
            if (bubbling.varyingBubbleSizes) {
                entityFactory.createVaryingBubbleCloud(pos,
                                                       numBubbles,
                                                       bubbling.bubbleDiam,
                                                       cloudDiam,
                                                       bubbling.bubbleLifetime_seconds);
            }
            else {
                entityFactory.createBubbleCloud(pos,
                                                numBubbles,
                                                bubbling.bubbleDiam,
                                                cloudDiam,
                                                bubbling.bubbleLifetime_seconds);
            }

            // Wait until next time for more
            if (bubbling.varyingInterval) {
                bubbling.secondsUntilNextBubbles = random.nextFloat() * bubbling.bubblingInterval_seconds + bubbling.bubblingInterval_seconds*0.5f;
            }
            else {
                bubbling.secondsUntilNextBubbles = bubbling.bubblingInterval_seconds + random.nextFloat() * 0.1f; // Add small random factor to avoid need for lots of simultaneous bubble object creation.
            }
        }
    }
}
