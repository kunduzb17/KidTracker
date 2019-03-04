package com.google.kaist.lavi.java.kidtracker;

public class KidMessage {
    private String id;
    private String location;
//    private String name;
    private String time;

    public KidMessage() {
    }

    public KidMessage(String location, String time) {
        this.id = "dummy_id";
        this.location = location;
        this.time = time;
    }

    public KidMessage(String id, String location, String time) {
        this.id = id;
        this.location = location;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
