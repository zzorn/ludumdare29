package org.ludumdare29.components;

import com.badlogic.gdx.Input;
import org.flowutils.MathUtils;
import org.ludumdare29.parts.Controllable;
import org.ludumdare29.parts.Tank;

/**
 * Component for ships
 */
public class ShipComponent extends SystemComponent {

    public final Controllable dieselEngineForwardThrust_N = controllable("Engine", -500000, 0, 5000000, 10, 6,
                                                                         Input.Keys.S, Input.Keys.W, 0.3f, false);

    public final Controllable rudder_turns_per_second = controllable("Rudder", -0.05f, 0, 0.05f, 10, 12,
                                                                     Input.Keys.A, Input.Keys.D, 0.1f, true);

    public final Tank dieselTank_l = tank("Diesel", 2000, 0.5f);

    public float dieselConsumptionAtFullThrottle_l_per_s = 0.02f;

    public ShipComponent() {
    }
}
