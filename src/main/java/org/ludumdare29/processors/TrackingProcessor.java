package org.ludumdare29.processors;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.time.Time;
import org.ludumdare29.components.LocationComponent;
import org.ludumdare29.components.TrackingComponent;

/**
 *
 */
public final class TrackingProcessor extends BaseEntityProcessor {

    private final Vector3 tempV = new Vector3();
    private final Quaternion tempQ = new Quaternion();


    public TrackingProcessor() {
        super(TrackingProcessor.class, TrackingComponent.class, LocationComponent.class);
    }

    @Override protected void processEntity(Time time, Entity entity) {
        final TrackingComponent tracking = entity.getComponent(TrackingComponent.class);
        final LocationComponent location = entity.getComponent(LocationComponent.class);

        final Entity trackedEntity = tracking.trackedEntity;
        if (trackedEntity != null) {
            final LocationComponent trackedLocation = trackedEntity.getComponent(LocationComponent.class);
            if (trackedLocation != null) {

                // Calculate relative position in target entity model space
                tempV.set(tracking.relativePosition);
                trackedLocation.direction.transform(tempV);

                // Add target entity pos
                tempV.add(trackedLocation.position);

                // Update location
                location.position.set(tempV);


                // Add relative direction
                tempQ.set(tracking.relativeDirection);
                tempQ.mul(trackedLocation.direction);

                // Update direction
                location.direction.set(tempQ);
            }
        }
    }
}
