package com.spirent.its.bgpmonitor;

public class Device {

    private String name;
    private String address;
    private String community;

    private DeviceManager deviceManager;

    private String deviceType;
    private String deviceStatus;

    public Device() {
        name = "";
        address = "";
        community = "";
        deviceType = "unknown";
        deviceStatus = "uninitialized";
        deviceManager = null;
    }

    // Getters and Setters
    public void setManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public DeviceManager getManager() {
        return deviceManager;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getCommunity() {
        return community;
    }

    public void setType(String type) {
        this.deviceType = type;
    }

    public String getType() {
        return this.deviceType;
    }

    public void setStatus(String status) {
        this.deviceStatus = status;
    }

    public String getStatus() {
        return this.deviceStatus;
    }

    @Override
    public String toString() {
        String newline = "\n";
        StringBuilder out = new StringBuilder();

        out.append(String.format("%-20s %-15s %s", this.name, this.address, this.community));
        out.append(String.format("  %-10s %-15s", this.deviceType, this.deviceStatus ));
        out.append(newline);

        return out.toString();
    }

}
