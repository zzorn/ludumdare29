package org.ludumdare29.components.appearance;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 * Spherical appearance
 */
public class BubbleAppearance extends ModelAppearance {

    private final Color color;

    public BubbleAppearance() {
        this(Color.GRAY);
    }

    public BubbleAppearance(Color color) {
        this.color = color;
    }

    @Override protected Model createBaseModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        return modelBuilder.createSphere(1, 1, 1,
                                         16, 16,
                                         new Material(),
                                         VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
    }


    @Override protected void configureInstance(ModelInstance appearance) {
        appearance.materials.get(0).set(ColorAttribute.createDiffuse(color));

        setScale(0);
    }
}
