package com.nana.wsalogin.helpers;

/**
 * Created by NanaYaw on 5/24/2017.
 */
public class Systems {
    int systemId;
    String location;
    String plant;
    boolean status;
    boolean pumpStatus;

    public String getLocation() {
        return location;
    }

    public Systems setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getPlant() {
        return plant;
    }

    public Systems setPlant(String plant) {
        this.plant = plant;
        return this;
    }

    public boolean isPumpStatus() {
        return pumpStatus;
    }

    public Systems setPumpStatus(boolean pump_status) {
        this.pumpStatus = pumpStatus;
        return this;
    }

    public boolean isStatus() {
        return status;
    }

    public Systems setStatus(boolean status) {
        this.status = status;
        return this;
    }

    public int getSystemId() {
        return systemId;
    }

    public Systems setSystemId(int systemId) {
        this.systemId = systemId;
        return this;
    }
}
