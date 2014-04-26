package org.ludumdare29.components.appearance;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 *
 */
public class SubmarineAppearance extends AppearanceComponent {

    private float width = 10f;
    private float length = 40f;
    private float relHeight = 1.4f;
    private float tailRelLength = 1.2f;

    private float towerRelPos = 0.3f;
    private float towerRelHeight = 0.8f;
    private float towerRelLength = 0.1f;
    private float towerRelWidth = 0.2f;

    private float aftFinRelPos = 0.9f;
    private float aftFinRelScale = 0.1f;
    private float aftFinWidth = 3f;
    private float aftFinLength = 2f;
    private float aftFinThickness = 0.2f;

    private float sailRelPos = 0.6f;
    private float sailFinRelScale = 0.1f;
    private float sailFinWidth = 4f;
    private float sailFinLength = 2f;
    private float sailFinThickness = 0.2f;

    private float antennaRelLength = 1f;
    private float antennaRelDiam = 0.1f;

    private float detailAmount = 0.1f;


    @Override protected ModelInstance createAppearance() {
        ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.begin();

        final int attributes = VertexAttributes.Usage.Position |
                               VertexAttributes.Usage.Normal |
                               VertexAttributes.Usage.TextureCoordinates;

        modelBuilder.part("hull", GL20.GL_TRIANGLES, attributes, new Material(ColorAttribute.createDiffuse(0.3f, 0.3f, 0.4f, 1f))).capsule(10, 40, 16);
        modelBuilder.part("tower", GL20.GL_TRIANGLES, attributes, new Material(ColorAttribute.createDiffuse(0.4f, 0.4f, 0.6f, 1f))).capsule(4, 60, 16);

        final Model model = modelBuilder.end();

        //node2.translation.set(10, 5, 0);

        //final Model model = modelBuilder.end();



        return new ModelInstance(model);
    }
}
