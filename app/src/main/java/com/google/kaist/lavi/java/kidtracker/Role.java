package com.google.kaist.lavi.java.kidtracker;

public class Role {

    private String role;
    private String hello;
    private String bye;


    public Role() {

    }

    public Role(String role, String hello, String bye) {
        this.role = role;
        this.hello = hello;
        this.bye = bye;

    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getHello() { return hello; }

    public void setHello(String hello) { this.hello = hello; }

    public String getBye() { return bye; }

    public void setBye(String bye) { this.bye = bye; }
}
