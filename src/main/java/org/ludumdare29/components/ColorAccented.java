package org.ludumdare29.components;

import com.badlogic.gdx.graphics.Color;
import org.entityflow.component.BaseComponent;

/**
 *
 */
public class ColorAccented extends BaseComponent {

    public Color accentColor;

    public ColorAccented(Color accentColor) {
        this.accentColor = accentColor;
    }
}
