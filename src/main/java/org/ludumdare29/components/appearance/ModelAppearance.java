package org.ludumdare29.components.appearance;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import org.entityflow.entity.Entity;

/**
 * Appearance based on a shared static model class.
 */
public abstract class ModelAppearance extends AppearanceComponent {

    private static Model sharedModel;

    @Override protected final ModelInstance createAppearance() {
        return new ModelInstance(getModel());
    }

    private Model getModel() {
        if (sharedModel == null) {
            sharedModel = createBaseModel();
        }

        return sharedModel;
    }

    protected abstract Model createBaseModel();


}
