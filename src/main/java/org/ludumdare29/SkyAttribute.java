package org.ludumdare29;

import com.badlogic.gdx.graphics.g3d.Attribute;

/**
 *
 */
public final class SkyAttribute extends Attribute {

    public final static String SKY_ATTRIBUTE_ALIAS = "Sky";
    public final static long SKY_ATTRIBUTE = register(SKY_ATTRIBUTE_ALIAS);


    public SkyAttribute(long type) {
        super(type);
    }

    public static SkyAttribute sky() {
        return new SkyAttribute(SKY_ATTRIBUTE);
    }

    @Override public Attribute copy() {
        return new SkyAttribute(type);
    }

    @Override protected boolean equals(Attribute other) {
        return SkyAttribute.class.isInstance(other);
    }
}
