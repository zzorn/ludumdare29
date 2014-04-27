package org.ludumdare29;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 *
 */
public class OceanShader extends BaseShader {

    private ShaderProgram program;
    private Camera camera;
    private RenderContext context;
    private int u_projTrans;
    private int u_worldTrans;
    private int u_color;


    @Override public void init() {
        String vert = Gdx.files.internal("assets/shaders/oceanshader.vertex.glsl").readString();
        String frag = Gdx.files.internal("assets/shaders/oceanshader.fragment.glsl").readString();
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled())
            throw new GdxRuntimeException(program.getLog());
        u_projTrans = program.getUniformLocation("u_projTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
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
        context.setDepthTest(GL20.GL_LEQUAL, 0.1f, 2000f);
        context.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render (Renderable renderable) {
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        program.setUniformf(u_color, MathUtils.random(), MathUtils.random(), MathUtils.random());
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