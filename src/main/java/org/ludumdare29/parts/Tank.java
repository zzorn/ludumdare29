package org.ludumdare29.parts;

import org.flowutils.Check;
import org.flowutils.MathUtils;

import static org.flowutils.MathUtils.*;

/**
 * A tank or other storage with something.
 */
public final class Tank {

    private String name;
    private float maxCapacity = 1;
    private float currentAmount = 0;
    private float change_per_second = 0;

    private float currentPos = 0;

    private float fullPos = 0.8f;
    private float warningPos = 0.3f;
    private float alarmPos = 0.15f;

    /**
     * @param name user readable name
     * @param maxCapacity maximum capacity for the tank.
     */
    public Tank(String name, float maxCapacity) {
        this(name, maxCapacity, 0);
    }

    /**
     * @param name user readable name
     * @param maxCapacity maximum capacity for the tank.
     * @param currentPos current relative fill level of the tank, 0 = empty, 1 = full.
     */
    public Tank(String name, float maxCapacity, float currentPos) {
        this(name, maxCapacity, currentPos, 0);
    }

    /**
     * @param name user readable name
     * @param maxCapacity maximum capacity for the tank.
     * @param currentPos current relative fill level of the tank, 0 = empty, 1 = full.
     * @param change_per_second how much the tank content changes per second.
     */
    public Tank(String name, float maxCapacity, float currentPos, float change_per_second) {
        setName(name);
        setMaxCapacity(maxCapacity);
        setCurrentPos(currentPos);
        setChange_per_second(change_per_second);
    }

    /**
     * @return relative level of the tank, 0 = empty, 1 = full.
     */
    public float getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(float currentPos) {
        this.currentPos = clamp0To1(currentPos);
        currentAmount = mix(currentPos, 0, maxCapacity);
    }

    public float getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(float currentAmount) {
        this.currentAmount = clamp(currentAmount, 0, maxCapacity);
        currentPos = map(currentAmount, 0, maxCapacity, 0, 1);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Check.nonEmptyString(name, "name");
        this.name = name;
    }

    public float getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(float maxCapacity) {
        Check.positiveOrZero(maxCapacity, "maxCapacity");
        this.maxCapacity = maxCapacity;
        setCurrentAmount(currentAmount);
    }

    public float getChange_per_second() {
        return change_per_second;
    }

    public void setChange_per_second(float change_per_second) {
        Check.normalNumber(change_per_second, "change_per_second");
        this.change_per_second = change_per_second;
    }

    /**
     * @param amountToTake take this amount away from the tank.
     * @return actual amount returned, may be less than the desired amount if the tank had too little left.
     */
    public float remove(float amountToTake) {
        Check.positiveOrZero(amountToTake, "amountToTake");
        if (amountToTake <= currentAmount) {
            changeCurrentAmount(-amountToTake);
            return amountToTake;
        } else {
            changeCurrentAmount(-currentAmount);
            return currentAmount;
        }
    }

    public float getFreeSpace() {
        return maxCapacity - currentAmount;
    }

    public boolean isEmpty() {
        return currentPos <= 0.0000001f;
    }

    public boolean isFull() {
        return currentPos >= 0.999999f;
    }

    /**
     * @param amountToAdd add this amount to the tank.
     * @return actual amount added, may be less than the desired amount if the tank had too little space left.
     */
    public float add(float amountToAdd) {
        Check.positiveOrZero(amountToAdd, "amountToAdd");
        final float freeSpace = getFreeSpace();
        if (amountToAdd <= freeSpace) {
            changeCurrentAmount(amountToAdd);
            return amountToAdd;
        } else {
            changeCurrentAmount(freeSpace);
            return freeSpace;
        }
    }

    public void changeCurrentAmount(float delta) {
        setCurrentAmount(currentAmount + delta);
    }

    public void update(float timeSinceLastCallSeconds) {
        changeCurrentAmount(change_per_second * timeSinceLastCallSeconds);
    }

    public AlarmStatus getAlarmStatus() {
        final float pos = getCurrentPos();
        if (pos <= alarmPos) return AlarmStatus.CRITICAL;
        else if (pos <= warningPos) return AlarmStatus.WARNING;
        else if (pos < fullPos) return AlarmStatus.OK;
        else return AlarmStatus.GREAT;
    }

}
