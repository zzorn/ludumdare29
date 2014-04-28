package org.ludumdare29.parts;

import org.flowutils.Check;

import static org.flowutils.Check.*;
import static org.flowutils.Check.notNull;
import static org.flowutils.MathUtils.*;

/**
 * Some controllable thing, e.g. engine thrust.
 * Has support for min and max value, desired value, actual target value, and current value.
 */
public final class Controllable {

    private String name;

    private float minValue;
    private float zeroValue;
    private float maxValue;

    private float controlLag_seconds;

    private boolean functional = true;
    private boolean jammed = false;

    private float currentValue;

    private float targetPos;
    private float currentPos;
    private float minPos;
    private float maxPos;

    private int controlSteps = 8;

    /**
     * A new controllable with a minimum value of -1, a zero value of 0, and a max value of 1, and 0 second controller lag.
     * @param name A name for this controllable object, for use in UI:s etc.
     */
    public Controllable(String name) {
        this.name = name;
    }

    /**
     * @param name A name for this controllable object, for use in UI:s etc.
     * @param zeroValue value for zero position (negative positions not allowed).
     * @param maxValue maximum value (for 1 position)
     * @param controlLag_seconds seconds it takes for the current value to change from zero to max.
     */
    public Controllable(String name, float zeroValue, float maxValue, float controlLag_seconds, int controlSteps) {
        this(name, zeroValue, zeroValue, maxValue, controlLag_seconds, false, controlSteps);
    }

    /**
     * @param name A name for this controllable object, for use in UI:s etc.
     * @param minValue minimum value (for -1 position)
     * @param zeroValue value for zero position
     * @param maxValue maximum value (for 1 position)
     * @param controlLag_seconds seconds it takes for the current value to change from zero to max.
     */
    public Controllable(String name, float minValue, float zeroValue, float maxValue, float controlLag_seconds, int controlSteps) {
        this(name, minValue, zeroValue, maxValue, controlLag_seconds, true, controlSteps);
    }

    private Controllable(String name,
                        float minValue,
                        float zeroValue,
                        float maxValue,
                        float controlLag_seconds,
                        boolean allowNegativeTargetPos,
                        int controlSteps) {
        setName(name);
        setControlLag_seconds(controlLag_seconds);
        setControlSteps(controlSteps);

        init(minValue, zeroValue, maxValue, allowNegativeTargetPos);
    }

    /**
     * @return A name for this controllable object, for use in UI:s etc.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name A name for this controllable object, for use in UI:s etc.
     */
    public void setName(String name) {
        nonEmptyString(name, "name");
        this.name = name;
    }

    /**
     * @param newTargetPos new position to move towards.  Should be in range -1..1 if the control is bi-directional, or 0..1 if unidirectional.
     */
    public void setTarget(float newTargetPos) {
        targetPos = clamp(newTargetPos, minPos, maxPos);
    }

    /**
     * @return user defined target pos.
     */
    public float getTargetPos() {
        return targetPos;
    }

    /**
     * Moves the target one step up.
     */
    public void increaseTarget() {
        moveTarget(true);
    }

    /**
     * Moves the target one step down.
     */
    public void decreaseTarget() {
        moveTarget(false);
    }

    /**
     * Moves the target one step up or downwards.
     *
     * @param increase if true, increase the target pos if possible, if false, decrease it.
     */
    public void moveTarget(boolean increase) {
        float stepDelta = 1f / controlSteps;
        setTarget(targetPos + stepDelta * (increase ? 1 : -1));
    }

    /**
     * @return controlSteps number of steps to go from zero to max, or zero to min, when using the moveTarget or increase/decrease target methods.
     */
    public int getControlSteps() {
        return controlSteps;
    }

    /**
     * @param controlSteps number of steps to go from zero to max, or zero to min, when using the moveTarget or increase/decrease target methods.
     */
    public void setControlSteps(int controlSteps) {
        Check.positive(controlSteps, "controlSteps");

        this.controlSteps = controlSteps;
    }

    /**
     * @return current actual value.
     */
    public float getCurrentValue() {
        return currentValue;
    }

    /**
     * @return current relative position.
     */
    public float getCurrentPos() {
        return currentPos;
    }

    /**
     * @return true if the controllable is operational, false if it is shut down or shutting down (going to zero).
     */
    public boolean isFunctional() {
        return functional;
    }

    /**
     * @param functional whether the controllable is operational, if set to true will go to the zero value regardless of user input.
     */
    public void setFunctional(boolean functional) {
        this.functional = functional;
    }

    /**
     * @return true if the controllable can not be moved by the user.
     */
    public boolean isJammed() {
        return jammed;
    }

    /**
     * @param jammed if true, changes made by the user will not move the current position.
     *               However, if the controllable becomes non-functional, its value will go to zero.
     */
    public void setJammed(boolean jammed) {
        this.jammed = jammed;
    }

    /**
     * Redefine the range for the adjusted value.
     * Resets the current value and position to zero.
     *
     * @param minValue minimum value (for -1 position)
     * @param zeroValue value for zero position
     * @param maxValue maximum value (for 1 position)
     */
    public void setValueRange(float minValue, float zeroValue, float maxValue) {
        init(minValue, zeroValue, maxValue, true);
    }

    /**
     * Redefine the range for the adjusted value.  Disabled negative values.
     * Resets the current value and position to zero.
     *
     * @param zeroValue value for zero position
     * @param maxValue maximum value (for 1 position)
     */
    public void setValueRange(float zeroValue, float maxValue) {
        init(zeroValue, zeroValue, maxValue, false);
    }

    /**
     * @return seconds it takes to go from the zero position to the full position after commanded so.
     */
    public float getControlLag_seconds() {
        return controlLag_seconds;
    }

    /**
     * @param controlLag_seconds seconds it takes to go from the zero position to the full position after commanded so.
     */
    public void setControlLag_seconds(float controlLag_seconds) {
        positiveOrZero(controlLag_seconds, "controlLag_seconds");
        this.controlLag_seconds = controlLag_seconds;
    }

    /**
     * @return absolute value of the current position.
     */
    public float getCurrentPosMagnitude() {
        return Math.abs(currentPos);
    }

    /**
     * Update current value depending on target over time.
     */
    public void update(float secondsSinceLastUpdate) {
        // Get position to move towards
        float actualTargetPos = calculateActualTargetPos();

        // Move towards the specified position
        updateCurrentPos(secondsSinceLastUpdate, actualTargetPos);

        // Update value based on new position
        updateCurrentValueFromCurrentPos();
    }

    /**
     * @return true if this controllable is currently functional and changing its value towards a target position.
     *         Can be used e.g. to turn a sound effect on or off.
     */
    public boolean changing() {
        return functional &&
               !jammed &&
               targetPos != currentPos;
    }

    private float calculateActualTargetPos() {
        float actualTargetPos;
        if (!functional) {
            // Go towards the zero pos if the controllable device is no longer functional
            actualTargetPos = 0;
        } else if (jammed) {
            // Can not be moved
            actualTargetPos = currentPos;
        } else {
            // Normally working, move towards target
            actualTargetPos = targetPos;
        }
        return actualTargetPos;
    }

    private void updateCurrentPos(float secondsSinceLastUpdate, float actualTargetPos) {
        if (controlLag_seconds == 0) {
            // No lag
            currentPos = actualTargetPos;
        }
        else {
            // Controller updating slowly to the target value
            float moveDelta = secondsSinceLastUpdate / controlLag_seconds;
            float diff = actualTargetPos - currentPos;
            if (Math.abs(diff) <= moveDelta) {
                currentPos = actualTargetPos;
            }
            else {
                currentPos += Math.signum(diff) * moveDelta;
            }
        }
    }

    private void updateCurrentValueFromCurrentPos() {
        if (currentPos >= 0) {
            currentValue = map(currentPos, 0, maxPos, zeroValue, maxValue);
        }
        else {
            currentValue = map(currentPos, minPos, 0, minValue, zeroValue);
        }
    }

    private void init(float minValue, float zeroValue, float maxValue, boolean allowNegativeTargetPos) {
        this.minValue = minValue;
        this.zeroValue = zeroValue;
        this.maxValue = maxValue;

        minPos = allowNegativeTargetPos ? -1 : 0;
        maxPos = 1;

        currentValue = zeroValue;

        targetPos = 0;
        currentPos = 0;
    }



}
