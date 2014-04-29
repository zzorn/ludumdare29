package org.ludumdare29.components;

import com.badlogic.gdx.Input;
import org.entityflow.component.BaseComponent;
import org.ludumdare29.parts.Controllable;
import org.ludumdare29.parts.Tank;

/**
 * Simple submersible component
 */
public class RocketComponent extends SystemComponent {

    public final Controllable engineThrust_N = controllable("Engine", 0, 1000000, 0, 5,
                                                                     Input.Keys.W, Input.Keys.S, 0.2f, false);
    public final Controllable diveFins_turns_per_sec = controllable(" Dive ", -0.07f, 0, 0.07f, 1, 8,
                                                                    Input.Keys.F, Input.Keys.R, 0.1f, false);
    public final Controllable rudderFins_turns_per_sec = controllable("Rudder", -0.07f, 0, 0.07f, 1, 8,
                                                                    Input.Keys.A, Input.Keys.D, 0.1f, false);

    public final Tank propellantTank_l = tank("Propellant", 200, 1f);

    public float propellantUsedOnFullThrust_l_per_s = 0.5f;

    public RocketComponent(float thrust_N) {
        engineThrust_N.setValueRange(0, thrust_N);
        engineThrust_N.setTarget(1);
    }
}
