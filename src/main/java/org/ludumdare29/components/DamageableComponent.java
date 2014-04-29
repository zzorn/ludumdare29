package org.ludumdare29.components;

import org.ludumdare29.parts.Tank;

/**
 *
 */
public class DamageableComponent extends SystemComponent {

    public final Tank hitpoints = new Tank("hitpoints", 1000, 1000, 1);

    public float debrisAmountOnDestruction = 100;

    public DamageableComponent(float hitpoints, float hpRegen, float debrisAmountOnDestruction) {
        this.debrisAmountOnDestruction = debrisAmountOnDestruction;

        this.hitpoints.setMaxCapacity(hitpoints);
        this.hitpoints.setCurrentAmount(hitpoints);
        this.hitpoints.setChange_per_second(hpRegen);

    }

    public void addDamage(float damage) {
        hitpoints.changeCurrentAmount(-damage);
    }

    public boolean isDestroyed() {
        return hitpoints.isEmpty();
    }
}
