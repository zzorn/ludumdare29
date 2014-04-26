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

        final Vector3 velocity = physical.velocity;
        final Vector3 position = location.position;
        final boolean underwater = sea.isUnderWater(position);

        // Calculate relative velocity in the fluid (water or air)
        sea.getCurrent(position, fluidVelocity);
        relativeVelocity.set(velocity).sub(fluidVelocity);

        // Apply water or air resistance
        // DragForce = -0.5 * surroundingDensity * velocityComparedToSurroundings^2 * entityDragConstant * entityCrossSection
        dragForce.set(relativeVelocity);
        float dragForceScale = -0.5f *
                               dragForce.len() * // Square velocity
                               sea.getDensity(position) * // Fluid density
                               physical.dragCoefficient *
                               physical.radius_m * physical.radius_m * MathUtils.TauFloat * 0.5f; // Area
        dragForce.scl(dragForceScale);
        dragForce.scl(deltaTime);
        velocity.add(dragForce);

        // Update movement based on engine acceleration (only works underwater)
        if (underwater) {
            velocityDelta.set(physical.acceleration);
            velocityDelta.scl(deltaTime);
            velocity.add(velocityDelta);
        }

        // Apply buoyancy
        float buoyancyForce = sea.getDensity(position) * physical.getVolume_m3() * sea.GRAVITY_AT_SEA_LEVEL;
        buoyancyForce *= deltaTime;
        velocity.y += buoyancyForce;

        // Update position
        positionDelta.set(velocity);
        positionDelta.scl(deltaTime);
        position.add(positionDelta);

        // Zero acceleration and thrust.  Any motor or other propulsion processors can update them.
        physical.acceleration.set(0,0,0);
        physical.torque.idt();
    }
}
