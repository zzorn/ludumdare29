package org.ludumdare29.processors;

import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.MathUtils;
import org.flowutils.SimplexGradientNoise;
import org.flowutils.time.Time;
import org.ludumdare29.Sea;
import org.ludumdare29.components.BubbleComponent;
import org.ludumdare29.components.LocationComponent;
import org.ludumdare29.components.PhysicalComponent;
import org.ludumdare29.components.appearance.AppearanceComponent;

import static org.flowutils.MathUtils.*;

/**
 *
 */
public class BubbleProcessor extends BaseEntityProcessor {

    public static final float BUBBLE_POP_DEPTH = 0.5f;
    private static final float BUBBLE_DRIFT_FORCE = 1f;
    private final Sea sea;

    private static final double BASIC_WOBBLES_PER_SEC = 0.5f;
    private static final float APPEARANCE_SCALE_FACTOR = 2; // Fudge factor to scale visible size

    public BubbleProcessor(Sea sea) {
        super(BubbleProcessor.class, BubbleComponent.class, LocationComponent.class, AppearanceComponent.class, PhysicalComponent.class);

        this.sea = sea;
    }

    @Override protected void processEntity(Time time, Entity entity) {
        final LocationComponent location = entity.getComponent(LocationComponent.class);
        final PhysicalComponent physical = entity.getComponent(PhysicalComponent.class);
        final AppearanceComponent appearance = entity.getComponent(AppearanceComponent.class);
        final BubbleComponent bubble = entity.getComponent(BubbleComponent.class);

        // Dissipate bubbles over time
        final float timeSinceLastStep = (float) time.getSecondsSinceLastStep();
        bubble.age_seconds += timeSinceLastStep;
        final float secondsLeft = bubble.getSecondsLeft();

        // Calculate density inside the bubble
        final float pressure      = sea.getPressure(location.position);
        final float temperature_k = sea.getTemperature_K(location.position);
        physical.density_kg_per_m3 = pressure / (temperature_k * Sea.DRY_AIR_SPECIFIC_GAS_CONSTANT);

        // Calculate bubble size from its mass and density, and age
        final float volume = physical.mass_kg / physical.density_kg_per_m3;
        final float radius = (float) Math.pow(volume / ((2.0 / 3.0) * Tau), 1.0/3.0);
        physical.radius_m = radius;

        // Update appearance size
        final float fadeOutFactor = MathUtils.mapAndClamp(secondsLeft, 5, 0, 1f, 0.000001f);
        final float fadeInFactor = MathUtils.mapAndClamp(bubble.age_seconds, 0, 2, 0.000001f, 1f);
        final float visibleScale = fadeInFactor * fadeOutFactor * radius * 2 * APPEARANCE_SCALE_FACTOR;
        appearance.setScale(visibleScale);

        // Wobble bubbles
        final double wobbleFrequency = BASIC_WOBBLES_PER_SEC * mapAndClamp(radius, 0, 1, 0.3f, 0.1f); // Larger wobble slower
        final float driftForce = BUBBLE_DRIFT_FORCE * mapAndClamp(radius, 0, 0.4f, 0.1f, 2f); // Larger bubbles drift more
        final float xDrift = driftForce * (float) SimplexGradientNoise.sdnoise1(time.getSecondsSinceStart() * wobbleFrequency + bubble.wobbleStart * 134.321f + 12.12f);
        final float yDrift = driftForce * (float) SimplexGradientNoise.sdnoise1(time.getSecondsSinceStart() * wobbleFrequency + bubble.wobbleStart * 732.132f + 43.32f);
        physical.acceleration.x += xDrift;
        physical.acceleration.z += yDrift;

        // Pop bubbles when they reach the surface, or when they dissipate
        if (secondsLeft <= 0f || sea.getDepth(location.position) <= BUBBLE_POP_DEPTH) {
            entity.delete();
        }
    }



}
