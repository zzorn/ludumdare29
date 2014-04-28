package org.ludumdare29.processors;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.MathUtils;
import org.flowutils.time.Time;
import org.ludumdare29.components.LocationComponent;
import org.ludumdare29.components.PhysicalComponent;
import org.ludumdare29.components.ShipComponent;

/**
 *
 */
public class ShipProcessor extends BaseEntityProcessor {

    private final Vector3 temp = new Vector3();
    private final Quaternion tempQ = new Quaternion();

    public ShipProcessor() {
        super(ShipProcessor.class, ShipComponent.class, LocationComponent.class, PhysicalComponent.class);
    }

    @Override protected void processEntity(Time time, Entity entity) {

        final float secondsSinceLastStep = (float) time.getSecondsSinceLastStep();

        final ShipComponent ship = entity.getComponent(ShipComponent.class);
        final PhysicalComponent physical = entity.getComponent(PhysicalComponent.class);
        final LocationComponent location = entity.getComponent(LocationComponent.class);

        // Update controls etc
        ship.update(secondsSinceLastStep);

        // Apply ship fuel consumption
        float fuelConsumption_l_per_s = ship.dieselConsumptionAtFullThrottle_l_per_s * ship.dieselEngineForwardThrust_N.getCurrentPosMagnitude();
        ship.dieselTank_l.remove(fuelConsumption_l_per_s * secondsSinceLastStep);

        // Check if we are out of fuel
        if (ship.dieselTank_l.isEmpty()) {
            ship.dieselEngineForwardThrust_N.setTarget(0);
        }

        // Apply thrust
        final float thrust_N = ship.dieselEngineForwardThrust_N.getCurrentValue();
        temp.set(-1, 0, 0);
        location.direction.transform(temp);
        physical.thrust.mulAdd(temp, thrust_N);

        // Apply turning
        // TODO: Could do torque later maybe
        tempQ.setFromAxisRad(0, 1, 0, MathUtils.TauFloat * ship.rudder_turns_per_second.getCurrentValue() * secondsSinceLastStep);
        tempQ.mul(location.direction);
        location.direction.set(tempQ);
    }
}
