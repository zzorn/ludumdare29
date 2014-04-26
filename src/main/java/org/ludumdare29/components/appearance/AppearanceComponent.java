package org.ludumdare29.components.appearance;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.component.BaseComponent;
import org.entityflow.entity.Entity;

/**
 * A 3D appearance for an entity.
 */
public abstract class AppearanceComponent extends BaseComponent {

    private ModelInstance appearance;
    private float scale = 1;
    private final Vector3 offset = new Vector3();


    protected AppearanceComponent() {
        super(AppearanceComponent.class);
    }

    public void setAppearance(ModelInstance appearance) {
        this.appearance = appearance;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Vector3 getOffset() {
        return offset;
    }

    public void setOffset(Vector3 offset) {
        this.offset.set(offset);
    }

    public void setOffset(float x, float y, float z) {
        this.offset.set(x, y, z);
    }

    public final ModelInstance getAppearance() {
        if (appearance == null) {
            appearance = createAppearance();

            configureInstance(appearance);
        }

        return appearance;
    }

    protected abstract ModelInstance createAppearance();

    protected void configureInstance(ModelInstance appearance) {
    }

    @Override protected void handleRemoved(Entity entity) {
        appearance.transform.scale(10, 1, 1);
    }
}
