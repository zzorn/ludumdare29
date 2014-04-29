package org.ludumdare29.processors;

import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.MathUtils;
import org.flowutils.time.Time;
import org.ludumdare29.EntityFactory;
import org.ludumdare29.components.DamageableComponent;
import org.ludumdare29.components.ExplodingComponent;
import org.ludumdare29.components.LocationComponent;

import javax.xml.stream.Location;

/**
 *
 */
public class ExplodingProcessor extends BaseEntityProcessor {

    private final EntityFactory entityFactory;

    /**
     * Do not constantly process all entities, as we do brute force proximity checking.
     */
    private static final float PROCESSING_INTERVAL_SECONDS = 0.2f;

    public ExplodingProcessor(EntityFactory entityFactory) {
        super(ExplodingProcessor.class, PROCESSING_INTERVAL_SECONDS, LocationComponent.class, ExplodingComponent.class, DamageableComponent.class);
        this.entityFactory = entityFactory;
    }

    @Override protected void processEntity(Time time, Entity entity) {
        final double lastStepDurationSeconds = time.getLastStepDurationSeconds();

        final ExplodingComponent exploding = entity.getComponent(ExplodingComponent.class);
        final LocationComponent explodingLocation = entity.getComponent(LocationComponent.class);
        if (exploding != null) {
            // Decrease timers
            exploding.secondsUntilArmed -= lastStepDurationSeconds;
            exploding.secondsUntilExplode -= lastStepDurationSeconds;

            if (exploding.shouldExplode()) {
                // Timer ended
                explode(entity);
            }
            else if (exploding.isArmed()) {
                // Scan nearby entities
                final Vector3 explodingPosition = explodingLocation.position;
                final float proximityRadius2 = exploding.proximityTriggerRadius_m * exploding.proximityTriggerRadius_m;
                for (Entity otherEntity : getHandledEntities()) {
                    if (otherEntity != entity &&
                        otherEntity != exploding.entityToIgnoreForProximity &&
                        otherEntity.containsComponent(DamageableComponent.class)) {

                        final LocationComponent entityLocation = entity.getComponent(LocationComponent.class);
                        if (entityLocation.position.dst2(explodingPosition) <= proximityRadius2 ) {
                            explode(entity);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void explode(Entity entity) {
        final ExplodingComponent exploding = entity.getComponent(ExplodingComponent.class);
        final LocationComponent explodingLocation = entity.getComponent(LocationComponent.class);

        final Vector3 explodingPosition = explodingLocation.position;

        // Scan for entities that get damaged
        float explosionRadius2 = exploding.damageRadius_m * exploding.damageRadius_m;
        for (Entity otherEntity : getHandledEntities()) {
            if (otherEntity != entity) {
                final DamageableComponent damageable = otherEntity.getComponent(DamageableComponent.class);
                final LocationComponent damageableLocation = otherEntity.getComponent(LocationComponent.class);
                if (damageable != null) {
                    float distance2 = explodingPosition.dst2(damageableLocation.position);
                    if (distance2 < explosionRadius2) {
                        // Add damage to the entity
                        float damage = MathUtils.map(distance2, 0, explosionRadius2, exploding.explosiveDamage, 0);
                        damageable.addDamage(damage);
                    }
                }
            }
        }

        // Spawn some bubbles
        entityFactory.createExplosion(explodingLocation.position, exploding.explosiveDamage, exploding.damageRadius_m);

        // Remove exploding entity
        entity.delete();
    }

    @Override protected boolean shouldHandle(Entity entity) {
        return entity.containsComponent(LocationComponent.class) && (
                entity.containsComponent(ExplodingComponent.class) ||
               entity.containsComponent(DamageableComponent.class));
    }
}
