package org.ludumdare29.processors;

import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.MathUtils;
import org.flowutils.time.Time;
import org.ludumdare29.Sea;
import org.ludumdare29.components.LocationComponent;
import org.ludumdare29.components.PhysicalComponent;

/**
 *
 */
public class PhysicsProcessor extends BaseEntityProcessor {

    private Sea sea;

    private final Vector3 velocityDelta = new Vector3();
    private final Vector3 positionDelta = new Vector3();
    private final Vector3 fluidVelocity = new Vector3();
    private final Vector3 relativeVelocity = new Vector3();
    private final Vector3 dragForce = new Vector3();

    public PhysicsProcessor(Sea sea) {
        super(PhysicsProcessor.class, LocationComponent.class, PhysicalComponent.class);
        this.sea = sea;
    }

    public Sea getSea() {
        return sea;
    }

    public void setSea(Sea sea) {
        this.sea = sea;
    }

    @Override protected void processEntity(Time time, Entity entity) {
        final float deltaTime = (float) time.getSecondsSinceLastStep();

        final LocationComponent location = entity.getComponent(LocationComponent.class);
        final PhysicalComponent physical = entity.getComponent(PhysicalComponent.class);

        final Vector3 force = physical.thrust;
        final Vector3 velocity = physical.velocity;
        final Vector3 position = location.position;
        final float environment_density = sea.getDensity(position);
        final float depth = sea.getDepth(position);
        final float radius_m = physical.getRadius_m();
        final float volume_m3 = physical.getVolume_m3();
        final float crossArea_m2 = physical.getCrossArea_m2();

        // Determine relative density of surroundings, depending on how much in the water / air the thing is
        final float surroundingDensity;
        if (depth <= -radius_m || depth >= radius_m) {
            // Completely in water or air
            surroundingDensity = environment_density;
        }
        else {
            // Part in water, part in air
            final float waterPart = (0.5f * depth / radius_m) + 0.5f;
            surroundingDensity = MathUtils.mix(waterPart, Sea.AIR_DENSITY_AT_SEA_LEVEL, Sea.SEA_DENSITY_AT_SEA_LEVEL);
        }


        // Calculate relative velocity in the fluid (water or air)
        sea.getCurrent(position, fluidVelocity);
        relativeVelocity.set(velocity).sub(fluidVelocity);

        // Apply buoyancy
        float buoyancyForce = surroundingDensity * volume_m3 * Sea.GRAVITY_AT_SEA_LEVEL;
        force.add(0, buoyancyForce, 0);

        // Apply gravitation
        float gravitationForce = physical.getMass_kg() * Sea.GRAVITY_AT_SEA_LEVEL;
        force.add(0, -gravitationForce, 0);

        // Update movement based on forces
        float movedMass = physical.getMass_kg() + 0.01f * crossArea_m2 * surroundingDensity; // Include some of the mass of the displaced medium, otherwise very light objects move too easily through a heavy medium
        force.scl(deltaTime / movedMass); // delta V = (Force * delta Time) / mass
        velocity.add(force);

        // Apply water or air resistance
        // DragForce = -0.5 * surroundingDensity * velocityComparedToSurroundings^2 * entityDragConstant * entityCrossSection
        float dragMagnitude = 0.5f *
                              relativeVelocity.len2() *  // Square velocity
                              surroundingDensity * // Fluid density
                              physical.dragCoefficient *
                              crossArea_m2;

        // Clamp drag so that it doesn't reverse the direction of travel
        dragMagnitude *= deltaTime / physical.getMass_kg();
        final float relativeVelocity = this.relativeVelocity.len();
        if (dragMagnitude > relativeVelocity) dragMagnitude = relativeVelocity;

        // Apply drag
        dragForce.set(this.relativeVelocity).nor();
        dragForce.scl(-dragMagnitude);
        velocity.add(dragForce);

        // Update position
        positionDelta.set(velocity);
        positionDelta.scl(deltaTime);
        position.add(positionDelta);

        // Zero thrust and torque.  Any motor or other propulsion processors can update them.
        physical.thrust.set(0,0,0);
        physical.torque.idt();
    }
}
