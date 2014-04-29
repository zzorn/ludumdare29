package org.ludumdare29.processors;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.MathUtils;
import org.flowutils.time.Time;
import org.ludumdare29.components.LocationComponent;
import org.ludumdare29.components.PhysicalComponent;
import org.ludumdare29.components.RocketComponent;

import static org.flowutils.MathUtils.TauFloat;

/**
 *
 */
public class RocketProcessor extends BaseEntityProcessor {


    private final Vector3 temp = new Vector3();
    private final Quaternion tempQ = new Quaternion();

    public RocketProcessor() {
        super(RocketProcessor.class, LocationComponent.class, PhysicalComponent.class, RocketComponent.class);
    }


    @Override protected void processEntity(Time time, Entity entity) {
        final LocationComponent location = entity.getComponent(LocationComponent.class);
        final PhysicalComponent physical = entity.getComponent(PhysicalComponent.class);
        final RocketComponent rocket = entity.getComponent(RocketComponent.class);

        final float secondsSinceLastStep = (float) time.getSecondsSinceLastStep();

        // Update controls etc
        rocket.update(secondsSinceLastStep);

        // Apply fuel consumption
        float fuelConsumption_l_per_s = rocket.propellantUsedOnFullThrust_l_per_s* rocket.engineThrust_N.getCurrentPosMagnitude();
        rocket.propellantTank_l.remove(fuelConsumption_l_per_s * secondsSinceLastStep);

        // Check if we are out of fuel
        if (rocket.propellantTank_l.isEmpty()) {
            rocket.engineThrust_N.setTarget(0);
        }

        // Apply thrust
        final float thrust_N = rocket.engineThrust_N.getCurrentValue();
        temp.set(-1, 0, 0);
        location.direction.transform(temp);
        physical.thrust.mulAdd(temp, thrust_N);

        // Apply turning
        tempQ.setFromAxisRad(0, 1, 0, MathUtils.TauFloat * rocket.rudderFins_turns_per_sec.getCurrentValue() * secondsSinceLastStep);
        tempQ.mul(location.direction);
        location.direction.set(tempQ);

        tempQ.setFromAxisRad(0, 0, 1, TauFloat * rocket.diveFins_turns_per_sec.getCurrentValue() * secondsSinceLastStep);
        location.direction.mul(tempQ);

        location.direction.nor();

    }
}
