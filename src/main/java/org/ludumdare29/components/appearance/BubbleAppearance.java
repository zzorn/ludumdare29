package org.ludumdare29.components.appearance;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import org.ludumdare29.shader.SpecialAttribute;

/**
 * Spherical appearance
 */
public class BubbleAppearance extends ModelAppearance {

    private static final Color DEFAULT_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.5f);
    private final Color color;

    public BubbleAppearance() {
        this(DEFAULT_COLOR);
    }

    public BubbleAppearance(Color color) {
        this.color = color;
    }

    @Override protected Model createBaseModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        return modelBuilder.createSphere(1, 1, 1,
                                         12, 12,
                                         new Material(SpecialAttribute.airBubble(),
                                                      new BlendingAttribute(true, 0.5f)),
                                         VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
    }


    @Override protected void configureInstance(ModelInstance appearance) {
        appearance.materials.get(0).set(ColorAttribute.createDiffuse(color));

        setScale(0);
    }
}
