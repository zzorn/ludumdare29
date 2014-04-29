package org.ludumdare29.processors;

import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.time.Time;
import org.ludumdare29.EntityFactory;
import org.ludumdare29.components.ColorAccented;
import org.ludumdare29.components.LocationComponent;
import org.ludumdare29.components.TorpedoTubeComponent;

/**
 *
 */
public class TorpedoTubeProcessor extends BaseEntityProcessor {

    private final EntityFactory entityFactory;

    public TorpedoTubeProcessor(EntityFactory entityFactory) {
        super(TorpedoTubeProcessor.class, LocationComponent.class, TorpedoTubeComponent.class, ColorAccented.class);
        this.entityFactory = entityFactory;
    }

    @Override protected void processEntity(Time time, Entity entity) {
        final LocationComponent location = entity.getComponent(LocationComponent.class);
        final TorpedoTubeComponent tube = entity.getComponent(TorpedoTubeComponent.class);
        final ColorAccented colorAccented = entity.getComponent(ColorAccented.class);

        tube.secondsUntilReloaded -= time.getLastStepDurationSeconds();

        if (tube.isLaunchRequested() && tube.isReadyToFire()) {
            entityFactory.createTorpedo(entity, location.position, location.direction, tube.torpedoSizeFactor, tube.torpedoSpeedFactor, colorAccented.accentColor);
            tube.secondsUntilReloaded = tube.reloadTime_s;
            tube.launchRequested = false;
        }

    }
}
