package org.ludumdare29.components;

import org.entityflow.component.BaseComponent;
import org.entityflow.entity.Entity;

/**
 *
 */
public class ExplodingComponent extends BaseComponent {

    public float secondsUntilArmed = 5;
    public float secondsUntilExplode = 60;
    public float proximityTriggerRadius_m = 10;
    public float explosiveDamage = 100;
    public float damageRadius_m = 100;
    public Entity entityToIgnoreForProximity;

    public Class<? extends BaseComponent> proximityTriggerComponentType = ShipComponent.class;

    public ExplodingComponent(Entity entityToIgnoreForProximity,
                              float secondsUntilExplode,
                              float secondsUntilArmed,
                              float proximityTriggerRadius_m,
                              float explosiveDamage,
                              float damageRadius_m) {
        this.entityToIgnoreForProximity = entityToIgnoreForProximity;
        this.secondsUntilExplode = secondsUntilExplode;
        this.secondsUntilArmed = secondsUntilArmed;
        this.proximityTriggerRadius_m = proximityTriggerRadius_m;
        this.explosiveDamage = explosiveDamage;
        this.damageRadius_m = damageRadius_m;
    }

    public boolean isArmed() {
        return secondsUntilArmed <= 0;
    }

    public boolean shouldExplode() {
        return secondsUntilExplode <= 0;
    }
}
