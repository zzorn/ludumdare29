package org.ludumdare29.components;

import org.entityflow.component.BaseComponent;

/**
 *
 */
public final class BubbleComponent extends BaseComponent {

    private static final int DEFAULT_LIFE_TIME = 60;

    public final double wobbleStart = Math.random();

    public float lifeTime_seconds;
    public float age_seconds = 0;

    public BubbleComponent() {
        this(DEFAULT_LIFE_TIME);
    }

    public BubbleComponent(float lifeTime_seconds) {
        this.lifeTime_seconds = lifeTime_seconds;
    }

    public float getSecondsLeft() {
        return lifeTime_seconds - age_seconds;
    }

}
