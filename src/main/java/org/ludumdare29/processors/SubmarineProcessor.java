package org.ludumdare29.processors;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.MathUtils;
import org.flowutils.time.Time;
import org.ludumdare29.Sea;
import org.ludumdare29.components.LocationComponent;
import org.ludumdare29.components.PhysicalComponent;
import org.ludumdare29.components.ShipComponent;
import org.ludumdare29.components.SubmarineComponent;
import org.ludumdare29.parts.AlarmStatus;

import static org.flowutils.MathUtils.*;

/**
 *
 */
public class SubmarineProcessor extends BaseEntityProcessor {

    private final Vector3 temp = new Vector3();
    private final Quaternion tempQ = new Quaternion();

    private final Sea sea;


    public SubmarineProcessor(Sea sea) {
        super(SubmarineProcessor.class, SubmarineComponent.class, ShipComponent.class, LocationComponent.class, PhysicalComponent.class);
        this.sea = sea;
    }

    @Override protected void processEntity(Time time, Entity entity) {

        final float secondsSinceLastStep = (float) time.getSecondsSinceLastStep();
        final float hoursSinceLastStep = secondsSinceLastStep / (60*60);

        final ShipComponent ship = entity.getComponent(ShipComponent.class);
        final SubmarineComponent submarine = entity.getComponent(SubmarineComponent.class);
        final PhysicalComponent physical = entity.getComponent(PhysicalComponent.class);
        final LocationComponent location = entity.getComponent(LocationComponent.class);

        // Update controls etc
        submarine.update(secondsSinceLastStep);

        boolean currentlyAtDiveDepth = sea.getDepth(location.position) > submarine.diveDepth_m;
        if (currentlyAtDiveDepth) {
            // Turn off diesel powered things if we are at dive depth
            ship.dieselEngineForwardThrust_N.setTarget(0);
            submarine.batteryChargeDelta_Wh_per_s.setTarget(0);
        }
        else {
            // Turn off electric drive on surface
            submarine.electricalMotorThrust_N.setTarget(0);
        }

        if (submarine.diveDepth != currentlyAtDiveDepth) {
            if (currentlyAtDiveDepth) {
                // When dived
            }
            else {
                // When surfaced

                // Turn on charger when surfacing, if we are low on power
                if (submarine.batteries_Wh.getAlarmStatus().getCriticality() >= AlarmStatus.WARNING.getCriticality()) {
                    submarine.batteryChargeDelta_Wh_per_s.setTarget(1);
                }
            }

            submarine.diveDepth = currentlyAtDiveDepth;
        }


        // Apply submarine fuel consumption
        float fuelConsumption_l_per_s = submarine.dieselConsumptionWhenCharging_l_per_s * submarine.batteryChargeDelta_Wh_per_s.getCurrentPos();
        ship.dieselTank_l.remove(fuelConsumption_l_per_s * secondsSinceLastStep);

        // Apply submarine electricity consumption
        float electricityConsumption_W = submarine.motorElectricityConsumptionAtFullThrottle_W * submarine.electricalMotorThrust_N.getCurrentPosMagnitude();
        electricityConsumption_W += submarine.lifeSupportElectricityConsumption_W;
        electricityConsumption_W += submarine.pumpElectricityConsumption_W * submarine.altitudeTankPumpSpeed_m3_per_s.getCurrentPosMagnitude();
        submarine.batteries_Wh.remove(electricityConsumption_W * hoursSinceLastStep);

        // Check if we are out of power
        if (submarine.batteries_Wh.isEmpty()) {
            submarine.electricalMotorThrust_N.setTarget(0);
            submarine.altitudeTankPumpSpeed_m3_per_s.setTarget(0);
        }

        // Update altitude tank
        submarine.altitudeTank_m3.changeCurrentAmount(submarine.altitudeTankPumpSpeed_m3_per_s.getCurrentValue() * secondsSinceLastStep);
        if ((submarine.altitudeTankPumpSpeed_m3_per_s.getTargetPos() > 0 && submarine.altitudeTank_m3.isFull() ||
            (submarine.altitudeTankPumpSpeed_m3_per_s.getTargetPos() < 0 && submarine.altitudeTank_m3.isEmpty()))) {
            // Shut pump off if we are done
            submarine.altitudeTankPumpSpeed_m3_per_s.setTarget(0);
        }

        // Update density
        float currentDensity_kg_per_m3 = map(submarine.altitudeTank_m3.getCurrentPos(),
                                             0, 1,
                                             submarine.minDensity_kg_per_m3,
                                             submarine.maxDensity_kg_per_m3);
        physical.setDensity_kg_per_m3(currentDensity_kg_per_m3);

        // Apply thrust
        final float thrust_N = submarine.electricalMotorThrust_N.getCurrentValue();
        temp.set(1, 0, 0);
        location.direction.transform(temp);
        physical.thrust.mulAdd(temp, thrust_N);

        // Slowly re-align the submarine to horizontal
        float alpha = secondsSinceLastStep / submarine.realignTime_s;
        tempQ.setEulerAngles(location.direction.getYaw(), 0, 0);
        //location.direction.set(tempQ);
        location.direction.slerp(tempQ, alpha);
        // TODO: Seems to align everything in one direction

        // Apply dive turning
        // TODO: Could do torque later maybe
        tempQ.setFromAxisRad(0, 0, 1, TauFloat * submarine.diveFins_turns_per_sec.getCurrentValue() * secondsSinceLastStep);
        location.direction.mul(tempQ);

        location.direction.nor();
    }
}
