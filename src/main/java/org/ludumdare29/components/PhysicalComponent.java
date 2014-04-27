package org.ludumdare29.components;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.component.BaseComponent;
import org.flowutils.MathUtils;

/**
 * Component with physics simulation applied.
 */
public final class PhysicalComponent extends BaseComponent {

    private static final float SPHERE_VOLUME_FACTOR = (2f / 3f) * MathUtils.TauFloat;
    private static final float CIRCLE_AREA_FACTOR = MathUtils.TauFloat * 0.5f;

    public final Vector3 velocity = new Vector3();
    public final Vector3 thrust = new Vector3();

    public final Quaternion rotation = new Quaternion();
    public final Quaternion torque = new Quaternion();

    private float mass_kg = 1f;
    private float density_kg_per_m3 = 1000f;
    private float radius_m = 1f;

    /**
     * Drag constant for this object.
     * Ranges from 0.04 for streamlined shapes to around 1 for bulky shapes.
     * A sphere has a dragCoefficient of 0.47 and a cube has 1.05 (when moving head on) or 0.8 (when angled).
     * See https://en.wikipedia.org/wiki/Drag_coefficient
     */
    public float dragCoefficient = 0.47f;

    public PhysicalComponent() {
        this(1, 1000);
    }

    public PhysicalComponent(float mass_kg,
                             float density_kg_per_m3) {
        this.mass_kg = mass_kg;
        setDensity_kg_per_m3(density_kg_per_m3);
    }

    public PhysicalComponent(float mass_kg,
                             float density_kg_per_m3,
                             float dragCoefficient) {
        this.mass_kg = mass_kg;
        setDensity_kg_per_m3(density_kg_per_m3);
        this.dragCoefficient = dragCoefficient;
    }

    /**
     * @return a new physical component with the specified radius and density.
     */
    public static PhysicalComponent fromRadiusAndDensity(float radius_m, float density_kg_per_m3) {
        final PhysicalComponent physicalComponent = new PhysicalComponent();

        float volume_m3 = SPHERE_VOLUME_FACTOR * radius_m * radius_m * radius_m;
        physicalComponent.mass_kg = volume_m3 * density_kg_per_m3;
        physicalComponent.radius_m = radius_m;
        physicalComponent.density_kg_per_m3 = density_kg_per_m3;

        return physicalComponent;
    }

    /**
     * @return a new physical component with the specified volume and density.
     */
    public static PhysicalComponent fromVolumeAndDensity(float volume_m3, float density_kg_per_m3) {
        final PhysicalComponent physicalComponent = new PhysicalComponent();

        physicalComponent.mass_kg = volume_m3 * density_kg_per_m3;
        physicalComponent.radius_m = calculateRadiusFromVolume(volume_m3);
        physicalComponent.density_kg_per_m3 = density_kg_per_m3;

        return physicalComponent;
    }

    /**
     * @return a new physical component with the specified mass and density.
     */
    public static PhysicalComponent fromMassAndDensity(float mass_kg, float density_kg_per_m3) {
        final PhysicalComponent physicalComponent = new PhysicalComponent();

        physicalComponent.mass_kg = mass_kg;
        physicalComponent.setDensity_kg_per_m3(density_kg_per_m3);

        return physicalComponent;
    }

    /**
     * @return a new physical component with the specified mass and radius.
     */
    public static PhysicalComponent fromMassAndRadius(float mass_kg, float radius_m) {
        final PhysicalComponent physicalComponent = new PhysicalComponent();

        physicalComponent.mass_kg = mass_kg;
        physicalComponent.setRadius_m(radius_m);

        return physicalComponent;
    }

    /**
     * @return a new physical component with the specified mass and volume.
     */
    public static PhysicalComponent fromMassAndVolume(float mass_kg, float volume_m3) {
        final PhysicalComponent physicalComponent = new PhysicalComponent();

        physicalComponent.mass_kg = mass_kg;
        physicalComponent.setVolume_m3(volume_m3);

        return physicalComponent;
    }

    /**
     * @return mass of the object.
     */
    public float getMass_kg() {
        return mass_kg;
    }

    /**
     * Sets the mass of the object.
     * Keeps the radius of the object, updates its density.
     */
    public void setMass_kg(float mass_kg) {
        this.mass_kg = mass_kg;
        density_kg_per_m3 = mass_kg / getVolume_m3();
    }

    /**
     * @return the density of the object.
     */
    public float getDensity_kg_per_m3() {
        return density_kg_per_m3;
    }

    /**
     * Updates the density of the object.
     * Keeps the mass, recalculates the radius.
     */
    public void setDensity_kg_per_m3(float density_kg_per_m3) {
        this.density_kg_per_m3 = density_kg_per_m3;

        float volume_m3 = mass_kg / density_kg_per_m3;
        radius_m = calculateRadiusFromVolume(volume_m3);
    }

    /**
     * Average radius of the object, in meters.
     * Calculated from mass and density.
     */
    public float getRadius_m() {
        return radius_m;
    }

    /**
     * Updates the radius of the objects.
     * Keeps the objects mass, recalculates its density.
     */
    public void setRadius_m(float radius_m) {
        this.radius_m = radius_m;

        // Recalculate density
        density_kg_per_m3 = mass_kg / getVolume_m3();
    }

    /**
     * Updates the radius of the object.
     * Keeps the objects density, recalculates its mass.
     */
    public void setRadiusChangeMass(float radius_m) {
        this.radius_m = radius_m;

        // Recalculate mass
        mass_kg = getVolume_m3() * density_kg_per_m3;
    }

    /**
     * Volume of the object in m^3.
     * Calculated from its radius.
     */
    public float getVolume_m3() {
        return SPHERE_VOLUME_FACTOR * radius_m * radius_m * radius_m;
    }

    /**
     * Sets the volume of the object.
     * Keeps its mass, recalculates its radius and density.
     */
    public void setVolume_m3(float volume_m3) {
        radius_m = calculateRadiusFromVolume(volume_m3);
        density_kg_per_m3 = mass_kg / volume_m3;
    }

    /**
     * @return cross sectional area of the object.  E.g. used for front facing area calculation for drag.
     */
    public float getCrossArea_m2() {
        return CIRCLE_AREA_FACTOR * radius_m * radius_m;
    }


    private static float calculateRadiusFromVolume(float volume_m3) {
        return (float) Math.pow(volume_m3 / SPHERE_VOLUME_FACTOR, 1.0 / 3.0);
    }
}
