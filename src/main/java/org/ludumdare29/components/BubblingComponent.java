package org.ludumdare29.components;

import com.badlogic.gdx.math.Vector3;
import org.entityflow.component.BaseComponent;

/**
 * Something that bubbles.
 */
public final class BubblingComponent extends BaseComponent {

    public int bubbleCount = 20;
    public float bubbleDiam = 0.05f;
    public float bubbleCloudDiam = 1f;
    public boolean varyingBubbleSizes = true;
    public boolean varyingBubbleCount = true;
    public float bubblingInterval_seconds = 10;
    public float bubbleLifetime_seconds = 20;
    public boolean varyingInterval = true;
    public boolean clusterBubbles = true;
    public Vector3 bubblingPosOffset = new Vector3();

    public float secondsUntilNextBubbles = (float) Math.random() * bubblingInterval_seconds;

    public BubblingComponent() {
        this(10, 20, 0.05f, 1);
    }

    public BubblingComponent(float bubblingInterval_seconds, int bubbleCount, float bubbleDiam, float bubbleCloudDiam) {
        this(bubblingInterval_seconds, bubbleCount, bubbleDiam, bubbleCloudDiam, bubblingInterval_seconds * 2f, true, true, true, false);
    }

    public BubblingComponent(float bubblingInterval_seconds,
                             int bubbleCount,
                             float bubbleDiam,
                             float bubbleCloudDiam,
                             float bubbleLifetime_seconds,
                             boolean varyingBubbleSizes,
                             boolean varyingBubbleCount,
                             boolean varyingInterval,
                             boolean clusterBubbles) {
        this.bubbleCount = bubbleCount;
        this.bubbleDiam = bubbleDiam;
        this.bubbleCloudDiam = bubbleCloudDiam;
        this.bubbleLifetime_seconds = bubbleLifetime_seconds;
        this.varyingBubbleSizes = varyingBubbleSizes;
        this.bubblingInterval_seconds = bubblingInterval_seconds;
        this.varyingBubbleCount = varyingBubbleCount;
        this.varyingInterval = varyingInterval;
        this.clusterBubbles = clusterBubbles;

        secondsUntilNextBubbles = (float) Math.random() * bubblingInterval_seconds;
    }
}
