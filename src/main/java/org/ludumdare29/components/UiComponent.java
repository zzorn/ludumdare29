package org.ludumdare29.components;

import org.entityflow.component.BaseComponent;

/**
 *
 */
public class UiComponent extends BaseComponent {

    public float relativeX = 0.1f;
    public float relativeY = 0.1f;
    public float alignX = 0;
    public float alignY = 0;

    public UiComponent() {
    }

    public UiComponent(float relativeX, float relativeY) {
        this.relativeX = relativeX;
        this.relativeY = relativeY;
    }

    public UiComponent(float relativeX, float relativeY, float alignX, float alignY) {
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.alignX = alignX;
        this.alignY = alignY;
    }
}
