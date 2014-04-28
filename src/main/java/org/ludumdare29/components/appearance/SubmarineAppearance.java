package org.ludumdare29.components.appearance;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

import static org.flowutils.MathUtils.*;

/**
 *
 */
public class SubmarineAppearance extends AppearanceComponent {

    private static final int DIVISIONS = 64;
    private static final Color DARK_TARGET = new Color(0,0,0.2f, 1);
    private static final Color LIGHT_TARGET = new Color(0.2f, 0.2f, 0f, 1f);
    public float length = 40f;
    public float width = 10f;
    public float relHeight = 1.4f;
    private float tailRelLength = 0.4f;
    private float relPropLength = 0.6f;
    private float relPropWidth = 0.5f;

    private float towerRelPos = 0.35f;
    private float towerRelHeight = 1.3f;
    private float towerRelLength = 1.1f;
    private float towerRelWidth = 0.6f;

    private float sailRelPos = 0.6f;
    private float sailFinRelScale = 0.1f;
    private float sailFinWidth = 2f;
    private float sailFinLength = 0.8f;
    private float sailFinThickness = 0.3f;

    private float aftFinRelPos = 1.3f;
    private float aftFinRelScale = 0.1f;
    private float aftFinWidth = 1.0f;
    private float aftFinLength = 0.5f;
    private float aftFinThickness = 0.05f;

    private float antennaRelLength = 1.5f;
    private float antennaRelDiam = 0.08f;

    private float detailAmount = 0.1f;

    private final Matrix4 tempTransform = new Matrix4();
    private Color color;
    private final Color accentColor;

    private static final Random random = new Random();

    public SubmarineAppearance() {
        this(40, 10);
    }

    public SubmarineAppearance(float length, float width) {
        this(length, width, new Color(0.3f, 0.3f, 0.4f, 1f), new Color(0.8f, 0.1f, 0.2f, 1f));
    }

    public SubmarineAppearance(float length, float width, Color color, Color accentColor) {
        this.length = length;
        this.width = width;
        this.color = color;
        this.accentColor = accentColor;
    }

    @Override protected ModelInstance createAppearance() {
        ModelBuilder modelBuilder = new ModelBuilder();

        modelBuilder.begin();

        final int attributes = VertexAttributes.Usage.Position |
                               VertexAttributes.Usage.Normal |
                               VertexAttributes.Usage.TextureCoordinates;

        final Material material = new Material(ColorAttribute.createDiffuse(color));
        final Material accentMaterial = new Material(ColorAttribute.createDiffuse(accentColor));
        final Material darkerMaterial = new Material(ColorAttribute.createDiffuse(color.cpy().lerp(DARK_TARGET, 0.2f)));
        final Material lighterMaterial = new Material(ColorAttribute.createDiffuse(color.cpy().lerp(LIGHT_TARGET, 0.2f)));

        // Body
        createPointedCapsule(modelBuilder, attributes, material, material, length, width, relHeight, tailRelLength, tempTransform.idt());

        // Tower
        final float towerWidth = towerRelWidth * width;
        final float towerHeight = towerRelHeight * width;
        final float towerLength = towerRelLength * width;
        final float towerPos = length * towerRelPos;
        createOvalCylinder(modelBuilder, attributes, material,
                           towerHeight,
                           towerWidth,
                           towerLength).translation.set(towerPos, width / 2, 0);

        // Color band on tower
        float accentHeight = mix(random.nextFloat(), 0.3f, 0.8f);
        float accentThickness = mix(random.nextFloat(), 1.01f, 1.2f);
        createOvalCylinder(modelBuilder, attributes, accentMaterial,
                           towerHeight * accentHeight,
                           towerWidth * accentThickness,
                           towerLength * accentThickness).translation.set(towerPos, width / 2, 0);

        // Tower fins
        fins(modelBuilder, attributes, darkerMaterial, towerWidth, towerHeight, sailFinWidth, sailFinLength, sailFinThickness, towerRelPos, sailRelPos, 0);

        // Aft fins
        fins(modelBuilder, attributes, darkerMaterial, width, width, aftFinWidth, aftFinLength, aftFinThickness, aftFinRelPos, 0.0f, 0);
        fins(modelBuilder, attributes, darkerMaterial, width, width, aftFinWidth, aftFinLength, aftFinThickness, aftFinRelPos, 0.0f, 0.25f);

        // Periscope / antennas
        createOvalCylinder(modelBuilder, attributes, lighterMaterial, width * antennaRelLength, width * antennaRelDiam, width * antennaRelDiam)
                .translation.set(towerPos + random.nextFloat() * towerLength/2-towerLength/4, width, random.nextFloat() * towerWidth/3);
        createOvalCylinder(modelBuilder, attributes, lighterMaterial, width * antennaRelLength*0.5f, width * antennaRelDiam*1.2f, width * antennaRelDiam)
                .translation.set(towerPos + random.nextFloat() * towerLength/2-towerLength/4, width, random.nextFloat() * towerWidth/3);

        final Model model = modelBuilder.end();

        // Center
        for (Node node : model.nodes) {
            node.translation.add(-length*0.5f, 0, 0);
        }

        return new ModelInstance(model);
    }

    private void fins(ModelBuilder modelBuilder,
                      int attributes,
                      Material material,
                      float baseWidth,
                      float baseHeight,
                      final float finWidth,
                      final float finLength,
                      final float finThickness,
                      final float finPosLength,
                      final float finPosHeight,
                      final float rollAngle) {
        final Node fin1 = createOvalCylinder(modelBuilder, attributes, material,
                                            finWidth * baseWidth,
                                            finThickness * baseWidth,
                                            finLength * width);
        final Node fin2 = createOvalCylinder(modelBuilder, attributes, material,
                                            finWidth * baseWidth,
                                            finThickness * baseWidth,
                                            finLength * width);
        fin1.rotation.setFromAxisRad(1, 0, 0, rollAngle * TauFloat + 0.25f * TauFloat);
        fin1.translation.set(length * finPosLength + baseWidth * 1 - baseWidth,
                             0.5f * 1 + baseHeight * finPosHeight,
                             0);

        fin2.rotation.setFromAxisRad(1, 0, 0, rollAngle * TauFloat -0.25f * TauFloat);
        fin2.translation.set(length * finPosLength + baseWidth * 1 - baseWidth,
                             0.5f * 1 + baseHeight * finPosHeight,
                             0);
    }


    private Node createOvalCylinder(ModelBuilder modelBuilder,
                                   int attributes,
                                   Material material,
                                   float height,
                                   float width,
                                   float length) {
        final Node node = modelBuilder.node();
        modelBuilder.part("part", GL20.GL_TRIANGLES, attributes, material).cylinder(length, height, width, DIVISIONS);
        //node.rotation.setFromAxisRad(0, 0, 1, 0.25f * TauFloat);
        //node.scale.set(1, aspect, 1);
        return node;
    }

    private void createPointedCapsule(ModelBuilder modelBuilder,
                                      int attributes,
                                      Material material,
                                      Material propMaterial,
                                      float length,
                                      float width,
                                      float aspect,
                                      float relPointLength,
                                      Matrix4 transformation) {
        // Main body
        final Node node = modelBuilder.node();
        modelBuilder.part("part", GL20.GL_TRIANGLES, attributes, material).capsule(width/2, length, DIVISIONS);
        node.rotation.setFromAxisRad(0, 0, 1, 0.25f * TauFloat);
        node.translation.set(length/2, 0, 0);
        node.scale.set(1, aspect, 1);
        node.localTransform.mul(transformation);

        // Aft
        final Node node2 = modelBuilder.node();
        final float coneLen = length * relPointLength;
        modelBuilder.part("part", GL20.GL_TRIANGLES, attributes, material).cone(width,
                                                                                coneLen,
                                                                                width,
                                                                                DIVISIONS);
        node2.rotation.setFromAxisRad(0, 0, 1, -0.25f * TauFloat);
        node2.translation.set(length + coneLen/2, 0, 0);
        node2.localTransform.mul(transformation);

        // Prop
        final Node node3 = modelBuilder.node();
        final float propLen = width * relPropLength;
        final float propWidth = width * relPropWidth;
        modelBuilder.part("part", GL20.GL_TRIANGLES, attributes, propMaterial).cylinder(propWidth,
                                                                                propLen,
                                                                                propWidth,
                                                                                DIVISIONS);
        node3.rotation.setFromAxisRad(0, 0, 1, 0.25f * TauFloat);
        node3.translation.set(length + coneLen - propLen/2, 0, 0);
        node3.localTransform.mul(transformation);
    }

    public Vector3 getPropellerOffset() {
        return new Vector3(length + length * tailRelLength * 0.7f, 0, 0);
    }

    public Vector3 getHatchOffset() {
        return new Vector3(length * towerRelPos - length * 0.5f, width * towerRelHeight * 1.1f, 0);
    }

    public Vector3 getCenterOffset() {
        return new Vector3(0, 0, 0);
    }
}
