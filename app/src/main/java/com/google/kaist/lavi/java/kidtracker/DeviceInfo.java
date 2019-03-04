package com.google.kaist.lavi.java.kidtracker;

public class DeviceInfo {

    private String mac;
    private Integer stage;

    public DeviceInfo() {

    }

    public DeviceInfo(String mac, Integer stage) {
        this.mac = mac;
        this.stage = stage;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

}
