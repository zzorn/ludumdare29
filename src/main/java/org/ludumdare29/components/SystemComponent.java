package org.ludumdare29.components;

import org.entityflow.component.BaseComponent;
import org.ludumdare29.parts.Controllable;
import org.ludumdare29.parts.Tank;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for components that have various controllable and other subsystems that need tobe updated.
 */
public abstract class SystemComponent extends BaseComponent {

    private List<Controllable> controllables = new ArrayList<>();
    private List<Tank> tanks = new ArrayList<>();

    protected final Controllable controllable(String name, float min, float zero, float max, float controlLag, int controlSteps) {
        final Controllable controllable = new Controllable(name, min, zero, max, controlLag, controlSteps);
        controllables.add(controllable);
        return controllable;
    }

    protected final Controllable controllable(String name, float zero, float max, float controlLag, int controlSteps) {
        final Controllable controllable = new Controllable(name, zero, max, controlLag, controlSteps);
        controllables.add(controllable);
        return controllable;
    }

    protected final Tank tank(String name, float max, float currentPos) {
        final Tank tank = new Tank(name, max, currentPos);
        tanks.add(tank);
        return tank;
    }

    protected final Tank tank(String name, float max, float currentPos, float changePerSecond) {
        final Tank tank = new Tank(name, max, currentPos, changePerSecond);
        tanks.add(tank);
        return tank;
    }

    public final void update(float secondsSinceLastCall) {
        for (Controllable controllable : controllables) {
            controllable.update(secondsSinceLastCall);
        }

        for (Tank tank : tanks) {
            tank.update(secondsSinceLastCall);
        }

        onUpdate(secondsSinceLastCall);
    }

    protected void onUpdate(float secondsSinceLastCall) {
    }


}
