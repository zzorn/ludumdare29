package org.ludumdare29.utils;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

/**
 *
 */
public final class AdvancedModelBuilder {

    private final static Pool<Vector3> vectorPool = new Pool<Vector3>() {
        @Override
        protected Vector3 newObject () {
            return new Vector3();
        }
    };

    private final ModelBuilder modelBuilder = new ModelBuilder();
    private int divisions = 32;
    private final Vector3 t = new Vector3();
    private final MeshPartBuilder.VertexInfo v = new MeshPartBuilder.VertexInfo();

    public AdvancedModelBuilder() {
        this(32);
    }

    public AdvancedModelBuilder(int divisions) {
        this.divisions = divisions;
        startModel();
    }

    public void startModel() {
        startModel(divisions);
    }

    public void startModel(int divisions) {
        this.divisions = divisions;
        modelBuilder.begin();
    }

    public Model finishModel() {
        return modelBuilder.end();
    }

    public void ring(Vector3 center, Vector3 sideways, float aspect) {


    }




}
