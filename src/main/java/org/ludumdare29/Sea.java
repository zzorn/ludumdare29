package org.ludumdare29;

import com.badlogic.gdx.math.Vector3;
import org.flowutils.SimplexGradientNoise;
import org.flowutils.gradient.Gradient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.flowutils.MathUtils.*;

/**
 * Models the sea (and air).
 */
public class Sea {

    public static final double ATMOSPHERIC_TEMPERATURE_LAPSE_RATE_K_PER_M = 0.0065;
    public static final double IDEAL_GAS_CONSTANT = 8.31447;
    public static final double DRY_AIR_MOLAR_MASS = 0.0289644;
    public static final float DRY_AIR_SPECIFIC_GAS_CONSTANT = 287.058f;
    public static final float ZERO_CELSIUS_K = 273.15f;
    public static final float GRAVITY_AT_SEA_LEVEL = 9.80665f;

    public static final float AIR_TEMPERATURE_AT_SEA_LEVEL_K = ZERO_CELSIUS_K + 15;
    public static final float WATER_TEMPERATURE_AT_SEA_LEVEL_K = AIR_TEMPERATURE_AT_SEA_LEVEL_K;
    public static final float AIR_PRESSURE_AT_SEA_LEVEL = 101.325f;
    public static final float AIR_DENSITY_AT_SEA_LEVEL = 1.25f;
    public static final float SEA_DENSITY_AT_SEA_LEVEL = 1026;
    public static final float SEA_DENSITY_INCREASE_PER_M = 0.004f;

    private final float seaLevel = 0;

    private static final Gradient seaTemperatureAtDepth_K = new Gradient(
            0, WATER_TEMPERATURE_AT_SEA_LEVEL_K,
            500, mix(0.7f, ZERO_CELSIUS_K + 6, WATER_TEMPERATURE_AT_SEA_LEVEL_K),
            1000,  ZERO_CELSIUS_K + 6,
            1500,  ZERO_CELSIUS_K + 5,
            3000,  ZERO_CELSIUS_K + 4,
            4000,  ZERO_CELSIUS_K + 3.5f,
            10000,  ZERO_CELSIUS_K + 1.5f
    );

    private final LayeredFlow waterFlow;

    public Sea() {
        waterFlow = new LayeredFlow(new Random(),
                                    300, 0.8,
                                    20, 0.3,
                                    2, 0.2,
                                    2000, 0.6,
                                    100, 0.1,
                                    10, 0.01,
                                    2, 7, 20, 100, 300, 800, 2000, 6000);
    }

    public boolean isUnderWater(Vector3 pos) {
        return getDepth(pos) > 0;
    }

    /**
     * @return sea level (y height of water surface) at the specified position.
     */
    public float getSeaLevel(Vector3 pos) {
        return seaLevel;
    }

    /**
     * @return water depth at the specified position.  Zero or negative if in the air.
     */
    public float getDepth(Vector3 pos) {
        return seaLevel - pos.y;
    }

    /**
     * Get the sea or air currents at the specified position.
     *
     * @param pos position to get current at
     * @param currentOut vector to store current velocity in
     * @return the current velocity at the specified position
     */
    public Vector3 getCurrent(Vector3 pos, Vector3 currentOut) {

        final float depth = getDepth(pos);

        if (depth > 0) {
            // Ocean currents
            waterFlow.getFlowXZ(pos, currentOut);
        }
        else {
            // No wind
            currentOut.set(0,0,0);
        }

        return currentOut;
    }

    /**
     * @return fluid density at the specified position, in kg/m^3.
     */
    public float getDensity(Vector3 pos) {

        float depth = getDepth(pos);

        if (depth <= 0) {
            return AIR_DENSITY_AT_SEA_LEVEL;
        }
        else {
            return SEA_DENSITY_AT_SEA_LEVEL + depth * SEA_DENSITY_INCREASE_PER_M;
        }
    }

    /**
     * @return fluid pressure at the specified position, in Pascal.
     */
    public float getPressure(Vector3 pos) {
        float depth = getDepth(pos);

        if (depth <= 0) {
            return AIR_PRESSURE_AT_SEA_LEVEL;
        }
        else {
            return AIR_PRESSURE_AT_SEA_LEVEL + getDensity(pos) * GRAVITY_AT_SEA_LEVEL * depth;
        }
    }

    /**
     * @return temperature at the specified position, in kelvin.
     */
    public float getTemperature_K(Vector3 pos) {
        float depth = getDepth(pos);

        if (depth <= 0) {
            return AIR_TEMPERATURE_AT_SEA_LEVEL_K;
        }
        else {
            return (float) seaTemperatureAtDepth_K.getValue(depth);
        }
    }

    private double calculateAirDensity(double altitude_m) {
        final double airPressureAtAltitude = calculateAirPressure(altitude_m);

        final double temperatureAtAltitude = AIR_TEMPERATURE_AT_SEA_LEVEL_K - ATMOSPHERIC_TEMPERATURE_LAPSE_RATE_K_PER_M * altitude_m;

        return airPressureAtAltitude * DRY_AIR_MOLAR_MASS / IDEAL_GAS_CONSTANT * temperatureAtAltitude;
    }

    private double calculateAirPressure(double altitude_m) {
        final double exponent = GRAVITY_AT_SEA_LEVEL * DRY_AIR_MOLAR_MASS / IDEAL_GAS_CONSTANT *
                                ATMOSPHERIC_TEMPERATURE_LAPSE_RATE_K_PER_M;

        final double base = AIR_PRESSURE_AT_SEA_LEVEL * (1.0 - ATMOSPHERIC_TEMPERATURE_LAPSE_RATE_K_PER_M * altitude_m /
                                                               AIR_TEMPERATURE_AT_SEA_LEVEL_K);

        return Math.pow(base, exponent);
    }


}
