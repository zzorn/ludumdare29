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

    public static final float BUBBLE_POP_DEPTH = 0.1f;
    private static final float BUBBLE_DRIFT_FORCE = 1f;
    private static final float MAX_SURFACE_BUBBLE_RADIUS = 0.2f;

    private static final float BUBBLE_FADE_OUT_SECONDS = 2f;
    private static final float BUBBLE_FADE_IN_SECONDS = 1f;

    private final Sea sea;

    private static final double BASIC_WOBBLES_PER_SEC = 1f;
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
        final float newDensity = pressure / (temperature_k * Sea.DRY_AIR_SPECIFIC_GAS_CONSTANT);
        physical.setDensity_kg_per_m3(Math.max(Sea.AIR_DENSITY_AT_SEA_LEVEL + 0.1f, newDensity));

        // Update appearance size
        final float radius = physical.getRadius_m();
        final float fadeInFactor = mapAndClamp(bubble.age_seconds, 0, BUBBLE_FADE_IN_SECONDS, 0.000001f, 1f);
        final float fadeOutFactor = mapAndClamp(secondsLeft, BUBBLE_FADE_OUT_SECONDS, 0, 1f, 0.000001f);
        final float visibleScale = fadeInFactor * fadeOutFactor * radius * 2 * APPEARANCE_SCALE_FACTOR;
        appearance.setScale(visibleScale);

        // Wobble rising bubbles
        if (!bubble.floating) {
            final double wobbleFrequency = BASIC_WOBBLES_PER_SEC * mapAndClamp(radius, 0, 1, 1f, 0.3f); // Larger wobble slower
            final float driftForce = BUBBLE_DRIFT_FORCE * mapAndClamp(radius, 0, 1f, 0f, 2f); // Larger bubbles drift more
            final float xDrift = driftForce * (float) SimplexGradientNoise.sdnoise1(time.getSecondsSinceStart() * wobbleFrequency + bubble.wobbleStart * 134.321f + 12.12f);
            final float yDrift = driftForce * (float) SimplexGradientNoise.sdnoise1(time.getSecondsSinceStart() * wobbleFrequency + bubble.wobbleStart * 732.132f + 43.32f);
            physical.thrust.x += xDrift;
            physical.thrust.z += yDrift;
        }

        // Pop bubbles when they reach the surface
        final float depth = sea.getDepth(location.position);
        if (!bubble.floating && depth <= 1) {
            bubble.floating = true;
            //bubble.age_seconds = bubble.lifeTime_seconds - 5;

            // Make large ones smaller
            if (physical.getRadius_m() > MAX_SURFACE_BUBBLE_RADIUS) {
                physical.setRadiusChangeMass(MAX_SURFACE_BUBBLE_RADIUS);
            }
        }

        // Lock floating bubbles to sea level
        if (bubble.floating) {
            //location.position.y = sea.getSeaLevel(location.position) - 0.01f;
            physical.velocity.y *= 0.1f;
        }


        // Destroy bubbles that are emitted above the surface
        // Remove bubbles when they dissipate
        if (depth < -1 || secondsLeft <= 0f) {
            entity.delete();
        }
    }



}
