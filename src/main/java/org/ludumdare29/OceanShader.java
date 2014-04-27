package org.ludumdare29;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 *
 */
public class OceanShader extends BaseShader {

    private ShaderProgram program;
    private Camera camera;
    private RenderContext context;
    private int u_projTrans;
    private int normalMatrixUniform;
    private int sunDirectionUniform;
    private int sunLightColorUniform;
    private int cameraPositionUniform;
    private int seaLevelUniform;
    private int isSkyUniform;
    private int u_worldTrans;
    private int u_color;

    private Matrix3 normalMatrix = new Matrix3();

    private final Sea sea;

    public OceanShader(Sea sea) {
        this.sea = sea;
    }


    @Override public void init() {
        String vert = Gdx.files.internal("assets/shaders/oceanshader.vertex.glsl").readString();
        String frag = Gdx.files.internal("assets/shaders/oceanshader.fragment.glsl").readString();

        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled())
            throw new GdxRuntimeException(program.getLog());

        u_projTrans = program.getUniformLocation("u_projTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        normalMatrixUniform = program.getUniformLocation("NormalMatrix");
        sunDirectionUniform = program.getUniformLocation("SunDirection");
        sunLightColorUniform = program.getUniformLocation("SunLightColor");
        cameraPositionUniform = program.getUniformLocation("CameraPosition");
        seaLevelUniform = program.getUniformLocation("SeaLevel");
        isSkyUniform = program.getUniformLocation("IsSky");
        u_color = program.getUniformLocation("u_color");
    }

    @Override public void dispose() {
        program.dispose();
    }

    @Override
    public void begin (Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        program.setUniformMatrix(u_projTrans, camera.combined);

        // Sun position and color
        program.setUniformf(sunDirectionUniform, sea.getSunDirection());
        program.setUniformf(sunLightColorUniform, sea.getSunLightColor());
        program.setUniformf(cameraPositionUniform, camera.position);
        program.setUniformf(seaLevelUniform, sea.getAverageSeaLevel());

        context.setDepthTest(GL20.GL_LEQUAL, 0.1f, 2000f);
        context.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render (Renderable renderable) {

        // Sky data
        boolean isSky = renderable.material.get(SkyAttribute.SKY_ATTRIBUTE) != null;
        program.setUniformi(isSkyUniform, isSky ? 1 : 0);

        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);

        normalMatrix.set(renderable.worldTransform);
        if (normalMatrix.det() == 0) {
            // Broken world matrix, can't be inverted
            normalMatrix.idt();
        }
        else {
            normalMatrix.inv().transpose();
        }

        program.setUniformMatrix(normalMatrixUniform, normalMatrix);

        Color diffuseColor = ((ColorAttribute)renderable.material.get(ColorAttribute.Diffuse)).color;
        program.setUniformf(u_color, diffuseColor);

        renderable.mesh.render(program,
                               renderable.primitiveType,
                               renderable.meshPartOffset,
                               renderable.meshPartSize);
    }

    @Override
    public void end () {
        program.end();
    }

    @Override
    public int compareTo (Shader other) {
        return 0;
    }
    @Override
    public boolean canRender (Renderable instance) {
        return true;
    }}
