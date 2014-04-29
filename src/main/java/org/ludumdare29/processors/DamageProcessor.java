package org.ludumdare29.processors;

import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.time.Time;
import org.ludumdare29.EntityFactory;
import org.ludumdare29.components.DamageableComponent;
import org.ludumdare29.components.LocationComponent;

/**
 *
 */
public class DamageProcessor extends BaseEntityProcessor {

    private final EntityFactory entityFactory;

    public DamageProcessor(EntityFactory entityFactory) {
        super(DamageProcessor.class, DamageableComponent.class, LocationComponent.class);
        this.entityFactory = entityFactory;
    }

    @Override protected void processEntity(Time time, Entity entity) {
        final DamageableComponent damageable = entity.getComponent(DamageableComponent.class);
        final LocationComponent location = entity.getComponent(LocationComponent.class);

        if (damageable.isDestroyed()) {

            entityFactory.createExplosion(location.position, damageable.debrisAmountOnDestruction, 20);

            entity.delete();
        }
    }
}
