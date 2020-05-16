package com.OpenNAC.openauth.Services;

public class User {


    private String fcmt;
    private String vendor;
    private String os;
    private String version;
    private String secret;
    private String factor;
    private String mac;
    private String result;
    private String token;
    private String reason;
    private String account;

    public User(String mac, String factor, String fcmt, String vendor, String os, String version) {
        this.factor = factor;
        this.mac = mac;
        this.fcmt = fcmt;
        this.vendor = vendor;
        this.os = os;
        this.version = version;
    }

    public String getAccount(){
        return account;
    }
    public String getResult() { return result; }

    public String getToken() { return token; }

    public String getOs() {
        return os;
    }

    public String getVersion() {
        return version;
    }

    public String getFactor() {
        return factor;
    }

    public String getMac() {
        return mac;
    }

    public String getFcmt() {
        return fcmt;
    }

    public String getVendor() {
        return vendor;
    }

    public String getSecret() {
        return secret;
    }

    public String getReason() {
        return reason;
    }
}
