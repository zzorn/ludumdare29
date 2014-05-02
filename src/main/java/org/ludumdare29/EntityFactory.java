package org.ludumdare29;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.world.World;
import org.ludumdare29.components.*;
import org.ludumdare29.components.appearance.*;
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

    public Entity createTorpedo(Entity sourceEntity, Vector3 pos, Quaternion direction, float sizeFactor, float speedFactor, Color accentColor) {
        // Location
        final LocationComponent location = new LocationComponent(pos, direction);

        // Appearance
        float length = mixAndClamp(sizeFactor + speedFactor / 2, 1, 15);
        float width = mixAndClamp(sizeFactor - speedFactor / 2, 2f, 8f);
        final Color baseColor = createBaseColor();
        final TorpedoAppearance appearance = new TorpedoAppearance(length, width, baseColor, accentColor);

        // Physical
        final float mass_kg = mix(sizeFactor, 100, 1000);
        final float density_kg_per_m3 = 900f;
        final float dragCoefficient = mix(speedFactor, 0.3f, 0.05f);
        final PhysicalComponent physical = new PhysicalComponent(mass_kg, density_kg_per_m3, dragCoefficient);
        if (sourceEntity != null) {
            final PhysicalComponent sourcePhysical = sourceEntity.getComponent(PhysicalComponent.class);
            physical.velocity.set(sourcePhysical.velocity);
        }

        // Explosive
        float lifetme = 20;
        float armTime= 1;
        float damage = mix(sizeFactor, 200, 1000);
        float damageRadius_m = mix(sizeFactor, 30, 100);
        float proximity_m = damageRadius_m * 0.4f;
        final ExplodingComponent exploding = new ExplodingComponent(sourceEntity,  lifetme, armTime, proximity_m, damage, damageRadius_m);

        // Rocket engine
        float thrust_N = mix(sizeFactor + speedFactor, 10000, 100000);
        final RocketComponent rocket = new RocketComponent(thrust_N);

        // Bubbles
        BubblingComponent bubbling = new BubblingComponent(0.05f, 10, 0.1f, 1, 2, true, false, false, false);
        //bubbling.bubblingPosOffset.set(appearance.getPropellerOffset());

        return world.createEntity(location, appearance, physical, exploding, rocket, bubbling);
    }

    public Entity createEnemySubmarine(Vector3 pos, float sizeFactor, float sleekness) {
        Color accentColor = new Color(0.95f, 0.05f * random.nextFloat(), 0.2f, 1);

        final Entity submarine = createSubmarine(pos, sizeFactor, sleekness, accentColor);
        submarine.addComponent(new EnemyAi());
        return submarine;
    }


    public Entity createSubmarine(Vector3 pos, float sizeFactor, float sleekness, Color accentColor) {
        LocationComponent location = new LocationComponent(pos);
        location.direction.setFromAxisRad(0, 1, 0, random.nextFloat() * TauFloat);

        final Color baseColor = createBaseColor();
        SubmarineAppearance appearance = new SubmarineAppearance(mixAndClamp(sizeFactor, 5f, 100f),
                                                                 mixAndClamp(sizeFactor, 3f, 16f) * mixAndClamp(sleekness, 1.5f, 0.5f),
                                                                 baseColor,
                                                                 accentColor);

        final float mass_kg = mixAndClamp(sizeFactor, 10000f, 100000f);
        final float dragCoefficient  = mixAndClamp(sleekness, 0.5f, 0.03f);
        PhysicalComponent physical = new PhysicalComponent(mass_kg, 1000f, dragCoefficient);

        BubblingComponent bubbling = new BubblingComponent(7, 30, 0.3f, appearance.width*0.5f, 15, true, true, true, false);
        bubbling.bubblingPosOffset.set(appearance.getPropellerOffset());

        SubmarineComponent submarine = new SubmarineComponent();

        ShipComponent ship = new ShipComponent();

        DamageableComponent damageable = new DamageableComponent(1000 + sizeFactor * 2000f - sleekness * 800,
                                                                 1f,
                                                                 sizeFactor);

        // Torpedo tube
        final float reloadTime_s = mixAndClamp(sizeFactor, 2f, 5);
        final float torpedoSizeFactor = mixAndClamp(sizeFactor, 0.1f, 1f);
        final float torpedoSpeedFactor = mixAndClamp( sizeFactor, 0.4f, 0.2f) + mixAndClamp(sleekness, 0.1f, 0.6f);
        final TorpedoTubeComponent torpedoTube = new TorpedoTubeComponent(reloadTime_s, torpedoSizeFactor, torpedoSpeedFactor);

        final ColorAccented colorAccented = new ColorAccented(accentColor);
        return world.createEntity(location, appearance, bubbling, physical, ship, submarine, damageable, torpedoTube, colorAccented);
    }

    private Color createBaseColor() {
        final Color baseColor = new Color(0.14f, 0.12f, 0.16f, 1f);
        baseColor.r *= 1f + (float) random.nextGaussian() * 0.03f;
        baseColor.g *= 1f + (float) random.nextGaussian() * 0.03f;
        baseColor.b *= 1f + (float) random.nextGaussian() * 0.03f;
        baseColor.mul(random.nextFloat() * 0.2f + 0.9f);
        baseColor.clamp();
        return baseColor;
    }

    public Entity createPlayerSubmarine(Vector3 pos, float sizeFactor, float sleekness, InputMultiplexer inputMultiplexer) {
        final Entity playerSubmarine = createSubmarine(pos, sizeFactor, sleekness, new Color(0.3f, 0.3f, 0.95f, 1f));

        final ShipComponent ship = playerSubmarine.getComponent(ShipComponent.class);
        final SubmarineComponent submarine = playerSubmarine.getComponent(SubmarineComponent.class);
        final TorpedoTubeComponent torpedoTube = playerSubmarine.getComponent(TorpedoTubeComponent.class);

        inputMultiplexer.addProcessor(ship.getInputHandler());
        inputMultiplexer.addProcessor(submarine.getInputHandler());
        inputMultiplexer.addProcessor(torpedoTube.getInputHandler());

        // Add a first person view camera
        world.createEntity(new LocationComponent(pos),
                           new CameraComponent(playerSubmarine, 80, false, true, 10),
                           new TrackingComponent(playerSubmarine, new Vector3(0, 6, 0)));

        // Add bridge view
        final Vector3 hatchOffset = ((SubmarineAppearance) playerSubmarine.getComponent(AppearanceComponent.class)).getHatchOffset();
        world.createEntity(new LocationComponent(pos),
                           new CameraComponent(null, 70, true, true),
                           new TrackingComponent(playerSubmarine, hatchOffset.cpy().add(-1, 0, 0)));

        // Add rear view camera
        world.createEntity(new LocationComponent(pos),
                           new CameraComponent(null, 90, true, false),
                           new TrackingComponent(playerSubmarine, new Vector3(80, 30, 0)));

        // Add a bubble cloud around the player to help visually orient them
        world.createEntity(new LocationComponent(pos),
                           new BubblingComponent(0.5f, 20, 0.05f, 200, 6, true, false, true, false),
                           new TrackingComponent(playerSubmarine, new Vector3(0, -20, 0)));


        // Setup UI
        float leftX = 0.1f;
        float rightX = 1f - leftX;
        float yStart = 0.2f;
        float ySpacing = 0.2f;
        float leftSupport =  90;
        float rightSupport = -90;
        world.createEntity(new UiComponent(leftX, yStart + 0 * ySpacing),
                           new GaugeAppearance(ship.dieselEngineForwardThrust_N, leftSupport, false));
        world.createEntity(new UiComponent(leftX, yStart + 1 * ySpacing),
                           new GaugeAppearance(submarine.electricalMotorThrust_N, leftSupport, false));

        world.createEntity(new UiComponent(leftX, yStart + 3 * ySpacing),
                           new GaugeAppearance(submarine.batteryChargeDelta_Wh_per_s, leftSupport, true));

        world.createEntity(new UiComponent(rightX, yStart + 0 * ySpacing),
                           new GaugeAppearance(ship.rudder_turns_per_second, rightSupport, false));
        world.createEntity(new UiComponent(rightX, yStart + 1 * ySpacing),
                           new GaugeAppearance(submarine.diveFins_turns_per_sec, rightSupport, true));
        world.createEntity(new UiComponent(rightX, yStart + 2 * ySpacing),
                           new GaugeAppearance(submarine.altitudeTankPumpSpeed_m3_per_s, rightSupport, false));


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

    public void createExplosion(Vector3 position, float explosiveDamage, float damageRadius_m) {
        float bubbleCount = mapAndClamp(explosiveDamage, 0, 1000, 3, 200);
        float bubbleSize = mapAndClamp(explosiveDamage, 0, 1000, 0.1f, 10f);
        float bubbleLifetime = mapAndClamp(explosiveDamage, 0, 1000, 4f, 16f);
        createVaryingBubbleCloud(position, (int) bubbleCount, bubbleSize, damageRadius_m, bubbleLifetime);
    }
}
