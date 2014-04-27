package org.ludumdare29;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.flowutils.MathUtils;

import java.util.Random;

/**
 * Layer cake of flows.
 */
public final class LayeredFlow {

    private final CurlNoise[] flows;
    private final float[] flowDepths;

    private final Vector3 prevLayerFlow = new Vector3();
    private final Vector3 nextLayerFlow = new Vector3();

    public LayeredFlow(Random random,
                       double surfaceRoughSize_m,
                       double surfaceFineSize_m,
                       double surfaceRoughAmplitude,
                       double surfaceFineAmplitude,
                       double bottomRoughSize_m,
                       double bottomFineSize_m,
                       double bottomRoughAmplitude,
                       double bottomFineAmplitude,
                       float ... flowDepths) {

        int flowCount = flowDepths.length;

        this.flowDepths = flowDepths;
        flows = new CurlNoise[flowCount];
        for (int i = 0; i < flowCount; i++) {
            double relPos = flowCount == 1 ? 0.5 : i / (flowCount-1);
            flows[i] = new CurlNoise(random,
                                     MathUtils.mix(relPos, surfaceRoughSize_m, bottomRoughSize_m),
                                     MathUtils.mix(relPos, surfaceFineSize_m, bottomFineSize_m),
                                     MathUtils.mix(relPos, surfaceRoughAmplitude, bottomRoughAmplitude),
                                     MathUtils.mix(relPos, surfaceFineAmplitude, bottomFineAmplitude));
        }
    }

    public void getFlowXZ(Vector3 pos, Vector3 flowOut) {
        float depth = pos.y;
        int i = 0;
        while (i < flowDepths.length && depth > flowDepths[i]) {
            i++;
        }

        if (i == 0 || i >= flowDepths.length) {
            // Top or bottom layer
            if (i >= flowDepths.length) i = flowDepths.length - 1;
            flows[i].getXZ(pos, flowOut);
        }
        else {
            // Mix previous and next layer

            flows[i - 1].getXZ(pos, prevLayerFlow);
            flows[i].getXZ(pos, nextLayerFlow);

            float prevDepth = flowDepths[i-1];
            float nextDepth = flowDepths[i];

            flowOut.x = MathUtils.map(depth, prevDepth, nextDepth, prevLayerFlow.x, nextLayerFlow.x);
            flowOut.y = 0;
            flowOut.z = MathUtils.map(depth, prevDepth, nextDepth, prevLayerFlow.z, nextLayerFlow.z);
        }

    }

}
