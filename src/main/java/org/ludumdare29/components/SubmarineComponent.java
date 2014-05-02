package org.ludumdare29.components;

import com.badlogic.gdx.Input;
import org.flowutils.MathUtils;
import org.ludumdare29.parts.Controllable;
import org.ludumdare29.parts.Tank;

/**
 * Component for submarines
 */
public class SubmarineComponent extends SystemComponent {

    public final Controllable electricalMotorThrust_N = controllable("Motor", -300000, 0, 700000, 5, 6,
                                                                     Input.Keys.F, Input.Keys.R, 0.2f, false);
    public final Controllable diveFins_turns_per_sec = controllable(" Dive ", -0.04f, 0, 0.04f, 1, 8,
                                                                    Input.Keys.S, Input.Keys.W, 0.1f, true);
    public final Controllable altitudeTankPumpSpeed_m3_per_s = controllable("Ballast", 0.2f, 0, -0.2f, 4, 3,
                                                                            Input.Keys.Q, Input.Keys.E, 0.2f, false);
    public final Controllable batteryChargeDelta_Wh_per_s = controllable("Charge Bat.", 0,                     10000, 6, 1,
                                                                         Input.Keys.C, Input.Keys.X, 1f, false);
    public final Tank altitudeTank_m3 = tank("Altitude Tank", 3, 0.5f);
    public final Tank batteries_Wh = tank("Batteries", 10000, 0.5f);

    public float motorElectricityConsumptionAtFullThrottle_W = 10000f;
    public float pumpElectricityConsumption_W = 1000f;
    public float lifeSupportElectricityConsumption_W = 1000f;
    public float dieselConsumptionWhenCharging_l_per_s = 0.01f;

    public float minDensity_kg_per_m3 = 800;
    public float maxDensity_kg_per_m3 = 1300;



    /**
     * Time in which the submarine re-aligns itself to be horizontal
     */
    public float realignTime_s = 10f;

    public float diveDepth_m = 10;

    public boolean diveDepth = false;

    public SubmarineComponent() {
    }
}
