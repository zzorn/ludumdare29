package org.ludumdare29.components;

import org.flowutils.MathUtils;
import org.ludumdare29.parts.Controllable;
import org.ludumdare29.parts.Tank;

/**
 * Component for ships
 */
public class ShipComponent extends SystemComponent {

    public final Controllable dieselEngineForwardThrust_N = controllable("Diesel Engine", -50000, 0, 200000, 10, 6);

    public final Controllable rudder_turns_per_second = controllable("Rudder", -0.04f, 0, 0.04f, 10, 8);

    public final Tank dieselTank_l = tank("Diesel", 200, 0.5f);

    public float dieselConsumptionAtFullThrottle_l_per_s = 0.1f;

    public ShipComponent() {
        dieselEngineForwardThrust_N.setTarget(1);
        rudder_turns_per_second.setTarget(-1);
    }
}
