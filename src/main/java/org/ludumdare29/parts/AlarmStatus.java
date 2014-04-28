package org.ludumdare29.parts;

/**
 *
 */
public enum AlarmStatus {
    GREAT(0),
    OK(1),
    WARNING(2),
    CRITICAL(3);

    private final int criticality;

    AlarmStatus(int criticality) {
        this.criticality = criticality;
    }

    public int getCriticality() {
        return criticality;
    }


}
