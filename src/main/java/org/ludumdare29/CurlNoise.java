package org.ludumdare29;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.flowutils.SimplexGradientNoise;

import java.util.Random;

/**
 * Noise for incompressible 2D flow.
 */
public final class CurlNoise {

    private static final Random RANDOM = new Random();

    private final double roughXOffset;
    private final double roughYOffset;
    private final double fineXOffset;
    private final double fineYOffset;

    private final double roughScale;
    private final double fineScale;
    private final double roughAmplitude;
    private final double fineAmplitude;

    private final double[] roughGradientOut = new double[2];
    private final double[] fineGradientOut = new double[2];

    public CurlNoise() {
        this(RANDOM);
    }

    public CurlNoise(Random random) {
        this(random, 1000, 10, 1, 0.1);
    }

    public CurlNoise(Random random, double roughSize_m, double fineSize_m, double roughAmplitude, double fineAmplitude) {
        // Random offsets
        roughXOffset = random.nextGaussian() * 3132.132112 + 5383.23372;
        roughYOffset = random.nextGaussian() * 1159.931023 + 1234.31552;
        fineXOffset  = random.nextGaussian() * 5428.345223 + 3123.31752;
        fineYOffset  = random.nextGaussian() * 7879.122334 + 9857.87123;

        this.roughScale = 1.0 / roughSize_m;
        this.fineScale = 1.0 / fineSize_m;
        this.roughAmplitude = roughAmplitude;
        this.fineAmplitude = fineAmplitude;
    }

    public void getXY(Vector2 pos, Vector2 flowOut) {
        SimplexGradientNoise.sdnoise2(pos.x * roughScale + roughXOffset,
                                      pos.y * roughScale + roughYOffset,
                                      roughGradientOut);
        SimplexGradientNoise.sdnoise2(pos.x * fineScale + fineXOffset,
                                      pos.y * fineScale + fineYOffset,
                                      fineGradientOut);

        // Rotate gradient 90 degrees to get incompressible flow perpendicular to the gradient slopes
        flowOut.x = (float) (roughGradientOut[1] * roughAmplitude +
                             fineGradientOut[1] * fineAmplitude);
        flowOut.y = - (float) (roughGradientOut[0] * roughAmplitude +
                               fineGradientOut[0] * fineAmplitude);
    }

    public void getXZ(Vector3 pos, Vector3 flowOut) {
        SimplexGradientNoise.sdnoise2(pos.x * roughScale + roughXOffset,
                                      pos.y * roughScale + roughYOffset,
                                      roughGradientOut);
        SimplexGradientNoise.sdnoise2(pos.x * fineScale + fineXOffset,
                                      pos.y * fineScale + fineYOffset,
                                      fineGradientOut);

        // Rotate gradient 90 degrees to get incompressible flow perpendicular to the gradient slopes
        flowOut.x = (float) (roughGradientOut[1] * roughAmplitude +
                             fineGradientOut[1] * fineAmplitude);
        flowOut.y = 0;
        flowOut.z = - (float) (roughGradientOut[0] * roughAmplitude +
                               fineGradientOut[0] * fineAmplitude);
    }

}
