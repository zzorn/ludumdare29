package org.ludumdare29.components;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.component.BaseComponent;
import org.entityflow.entity.Entity;

/**
 * Something that tracks another entitys position
 */
public class TrackingComponent extends BaseComponent {

    public Entity trackedEntity;

    public Vector3 relativePosition = new Vector3();
    public Quaternion relativeDirection = new Quaternion();

    public TrackingComponent(Entity trackedEntity) {
        this.trackedEntity = trackedEntity;
    }


    public TrackingComponent(Entity trackedEntity, Vector3 relativePosition) {
        this.trackedEntity = trackedEntity;
        this.relativePosition = relativePosition;
    }

    public TrackingComponent(Entity trackedEntity,
                             Vector3 relativePosition,
                             Quaternion relativeDirection) {
        this.trackedEntity = trackedEntity;
        this.relativePosition = relativePosition;
        this.relativeDirection = relativeDirection;
    }
}
