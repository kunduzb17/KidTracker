package com.google.kaist.lavi.java.kidtracker;

import java.util.List;

public class BeaconInfo {

    private List<String> auth;
    private Integer stage;

    public BeaconInfo() {

    }

    public BeaconInfo(List<String> auth, Integer stage) {
        this.auth = auth;
        this.stage = stage;
    }

    public List<String> getAuth() {
        return auth;
    }

    public void setAuth(List<String> auth) {
        this.auth = auth;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }
}
