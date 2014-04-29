package org.ludumdare29.components.appearance;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import org.flowutils.MathUtils;
import org.flowutils.time.Time;
import org.ludumdare29.parts.Controllable;
import org.ludumdare29.utils.TextGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class GaugeAppearance extends AppearanceComponent {

    private static final Color BG_COLOR = new Color(0.25f, 0.22f, 0.18f, 1f);
    private static final Color PANEL_COLOR = new Color(0.12f, 0.12f, 0.12f, 1f);
    private static final Color BG_COLOR_SPEC = new Color(0.5f, 0.5f, 0.5f, 1f);
    private static final Color TEXT_COLOR = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final Color TARGE_NEEDLE_COLOR = new Color(0.7f, 0.2f, 0.2f, 1f);
    private static final Color TARGE_NEEDLE_SPEC = new Color(0.9f, 0.4f, 0.4f, 1f);
    private static final int ACCURACY = 64;
    private float diameter = 0.20f;
    private float depth = 0.1f;
    private float minDegree = -120;
    private float maxDegree =  120;
    private float supportAngleDeg =  -90;
    private boolean invertDirection = false;

    private static TextGenerator textGenerator = new TextGenerator();


    private Controllable visualizedControllable;

    private static final int ATTRIBUTES = VertexAttributes.Usage.Position |
                                          VertexAttributes.Usage.Normal |
                                          VertexAttributes.Usage.TextureCoordinates;
    private ModelInstance actualNeedle;
    private ModelInstance targetNeedle;

    public GaugeAppearance() {
    }

    public GaugeAppearance(Controllable visualizedControllable,
                           float supportAngleDeg,
                           boolean invertDirection) {
        this.visualizedControllable = visualizedControllable;
        this.supportAngleDeg = supportAngleDeg;
        this.invertDirection = invertDirection;
    }



    public GaugeAppearance(Controllable visualizedControllable,
                           float supportAngleDeg,
                           float diameter,
                           float depth,
                           float minDegree,
                           float maxDegree,
                           boolean invertDirection) {
        this.visualizedControllable = visualizedControllable;
        this.diameter = diameter;
        this.depth = depth;
        this.supportAngleDeg = supportAngleDeg;
        this.minDegree = minDegree;
        this.maxDegree = maxDegree;
        this.invertDirection = invertDirection;
    }

    @Override protected List<ModelInstance> createAppearances() {

        List<ModelInstance> models = new ArrayList<>(2);

        final Material baseMaterial = new Material(ColorAttribute.createDiffuse(BG_COLOR), ColorAttribute.createSpecular(BG_COLOR_SPEC));
        final Material needleMaterial = new Material(ColorAttribute.createDiffuse(TEXT_COLOR), ColorAttribute.createSpecular(TEXT_COLOR));
        final Material targetNeedleMaterial = new Material(ColorAttribute.createDiffuse(TARGE_NEEDLE_COLOR), ColorAttribute.createSpecular(TARGE_NEEDLE_SPEC));
        final Material panelMaterial = new Material(ColorAttribute.createDiffuse(PANEL_COLOR));

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        // Body
        modelBuilder.node();
        float d2 = diameter * 1.1f;
        float h2 = depth * 0.4f;
        float panelDiam = diameter * 0.9f;
        float panelH = depth * 1.03f;
        createPart(modelBuilder, baseMaterial).cylinder(diameter, depth, diameter, ACCURACY);
        createPart(modelBuilder, baseMaterial).cylinder(d2, h2, d2, ACCURACY);
        createPart(modelBuilder, panelMaterial).cylinder(panelDiam, panelH, panelDiam, ACCURACY);

        // Center knob
        float knobD = diameter * 0.2f;
        float knobH = depth * 1.3f;
        createPart(modelBuilder, baseMaterial).cylinder(knobD, knobH, knobD, ACCURACY);

        // Support
        final Node support = modelBuilder.node();
        float supportDiam = depth * 0.8f;
        float supportLen = diameter*2;
        final MeshPartBuilder part = createPart(modelBuilder, baseMaterial,
                                                0, 0, -supportLen / 2,
                                                1, 1, 1,
                                                1, 0, 0, 90);
        part.cylinder(supportDiam, supportLen, supportDiam, ACCURACY);
        support.rotation.setFromAxis(0, 1, 0, supportAngleDeg);

        // Text face
        modelBuilder.node();
        float labelW = diameter*1.4f;
        float labelH = diameter * 0.5f;
        float labelD = depth * 0.5f;
        float labelX = 0;
        float labelY = 0;
        float labelZ = diameter * 0.65f;
        createPart(modelBuilder, baseMaterial).box(labelX, labelY, labelZ, labelW, labelD, labelH);
        float charH = labelH * 0.8f;
        float textW = labelW * 0.9f;
        float charT = labelD * 0.01f;
        textGenerator.createTextLine(modelBuilder, " " + visualizedControllable.getLabel() + " ",
                                     labelX, labelY + labelD*0.5f, labelZ,
                                     textW, charH, charT, true, true, false, true);

        // Key shortcut labels
        generateKeyLabel(modelBuilder, visualizedControllable.getLabelForDecrease(), invertDirection ? -1 : 1);
        generateKeyLabel(modelBuilder, visualizedControllable.getLabelForIncrease(), invertDirection ? 1 : -1);

        // Basic frame model
        models.add(new ModelInstance(modelBuilder.end()));

        // Current value needle
        actualNeedle = createNeedleModel(modelBuilder, needleMaterial, diameter*0.45f, diameter * 0.06f);
        models.add(actualNeedle);

        // Targevalue needle
        targetNeedle = createNeedleModel(modelBuilder, targetNeedleMaterial, diameter*0.25f, diameter * 0.12f);
        models.add(targetNeedle);

        return models;
    }

    private void generateKeyLabel(ModelBuilder modelBuilder, String decLabel, float xScale) {
        float keyLabelX = diameter * 0.15f * xScale;
        float keyLabelY = 0;
        float keyLabelZ = diameter * 0.2f;
        float keyLabelW = diameter * 0.3f;
        float keyLabelH = diameter * 0.22f;
        float keyLabelD = depth * 0.53f;

        // Pad single keys
        if (decLabel.length() == 1) decLabel = " " + decLabel + " ";

        textGenerator.createTextLine(modelBuilder, decLabel,
                                     keyLabelX, keyLabelY, keyLabelZ,
                                     keyLabelW, keyLabelH, keyLabelD,
                                     true, true, false,
                                     true);
    }


    private ModelInstance createNeedleModel(ModelBuilder modelBuilder, Material needleMaterial,
                                            float needleLen, float needleDiam) {
        modelBuilder.begin();
        modelBuilder.node();
        createPart(modelBuilder, needleMaterial,
                   0, 0.5f * depth, -0.5f * needleLen,
                   1, 1, 1,
                   1, 0, 0, 90).cylinder(needleDiam, needleLen, needleDiam, 8);

        return new ModelInstance(modelBuilder.end());
    }

    private MeshPartBuilder createPart(ModelBuilder modelBuilder, Material material) {
        return createPart(modelBuilder, material, 0,0,0);
    }

    private MeshPartBuilder createPart(ModelBuilder modelBuilder, Material material,
                                       float x, float y, float z) {
        return createPart(modelBuilder, material, x, y, z, 1);
    }

    private MeshPartBuilder createPart(ModelBuilder modelBuilder, Material material,
                                       float x, float y, float z, float scale) {
        return createPart(modelBuilder, material, x, y, z, scale, scale, scale);
    }

    private MeshPartBuilder createPart(ModelBuilder modelBuilder, Material material,
                                       float x, float y, float z,
                                       float scaleX, float scaleY, float scaleZ) {
        return createPart(modelBuilder, material, x, y, z, scaleX, scaleY, scaleZ, 0, 1, 0, 0);
    }

    private MeshPartBuilder createPart(ModelBuilder modelBuilder, Material material,
                                       float x, float y, float z,
                                       float scaleX, float scaleY, float scaleZ,
                                       float rotAxisX, float rotAxisY, float rotAxisZ, float rotationDegrees) {
        final MeshPartBuilder part = modelBuilder.part("part", GL20.GL_TRIANGLES, ATTRIBUTES, material);

        final Matrix4 transform = new Matrix4();
        transform.scale(scaleX, scaleY, scaleZ);
        transform.rotate(rotAxisX, rotAxisY, rotAxisZ, rotationDegrees);
        transform.setTranslation(x, y, z);
        part.setVertexTransform(transform);

        return part;
    }

    @Override public void update(Time time) {
        if (visualizedControllable != null && actualNeedle != null) {
            updateNeedleAngle(actualNeedle, visualizedControllable.getCurrentPos());
            updateNeedleAngle(targetNeedle, visualizedControllable.getTargetPos());
        }
    }

    private float updateNeedleAngle(final ModelInstance needle, final float pos) {
        float actualNeedleAngle;
        if (invertDirection) {
            actualNeedleAngle = MathUtils.map(pos,
                                              visualizedControllable.getMinPos(),
                                              visualizedControllable.getMaxPos(),
                                              maxDegree,
                                              minDegree);
        }
        else {
            actualNeedleAngle = MathUtils.map(pos,
                                              visualizedControllable.getMinPos(),
                                              visualizedControllable.getMaxPos(),
                                              minDegree,
                                              maxDegree);
        }

        needle.nodes.get(0).rotation.setFromAxis(0, 1, 0, actualNeedleAngle);
        needle.calculateTransforms();
        return actualNeedleAngle;
    }
}
