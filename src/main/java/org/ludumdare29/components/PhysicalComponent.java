package org.ludumdare29.components;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.component.BaseComponent;

/**
 * Component with physics simulation applied.
 */
public class PhysicalComponent extends BaseComponent {

    public final Vector3 velocity = new Vector3();
    public final Vector3 acceleration = new Vector3();

    public final Quaternion rotation = new Quaternion();
    public final Quaternion torque = new Quaternion();

    public float mass_kg = 1f;
    public float density_kg_per_m3 = 1000f;

    /**
     * Drag constant for this object.
     * Ranges from 0.04 for streamlined shapes to around 1 for bulky shapes.
     * A sphere has a dragCoefficient of 0.47 and a cube has 1.05 (when moving head on) or 0.8 (when angled).
     * See https://en.wikipedia.org/wiki/Drag_coefficient
     */
    public float dragCoefficient = 0.5f;

    /**
     * Rough average radius of the object, in meters.
     * Usable for collision detection or drag calculations, but not for calculating density.
     */
    public float radius_m = 1f;

    /**
     * Volume of the object in m^3.
     * Calculated from its mass and density.
     */
    public float getVolume_m3() {
        return mass_kg / density_kg_per_m3 ;
    }

    public PhysicalComponent() {
    }

    public PhysicalComponent(float mass_kg,
                             float radius_m) {
        this.mass_kg = mass_kg;
        this.radius_m = radius_m;
    }

    public PhysicalComponent(float mass_kg,
                             float radius_m,
                             float density_kg_per_m3,
                             float dragCoefficient) {
        this.mass_kg = mass_kg;
        this.radius_m = radius_m;
        this.density_kg_per_m3 = density_kg_per_m3;
        this.dragCoefficient = dragCoefficient;
    }

    /**
     * @return a new spherical physical component with the specified radius and density.
     */
    public static PhysicalComponent spherical(float radius_m,
                                              float density_kg_per_m3) {
        final PhysicalComponent physicalComponent = new PhysicalComponent();
        final float volume_m3 = (float) (4.0/3.0 * Math.PI * radius_m*radius_m*radius_m);
        physicalComponent.mass_kg = volume_m3 * density_kg_per_m3;
        physicalComponent.density_kg_per_m3 = density_kg_per_m3;
        physicalComponent.radius_m = radius_m;
        physicalComponent.dragCoefficient = 0.47f;

        return physicalComponent;
    }
}
