package com.OpenNAC.openauth.remote;

import com.google.gson.annotations.SerializedName;

public class Post {
    private String username;
    private String password;
    private boolean useOnlyLocalRepo;
    private String result;
    private String token;
    private String body;
    private String mac;
    public Post(String username, String password, boolean useOnlyLocalRepo){
            this.username = username;
            this.password = password;
            this.useOnlyLocalRepo = useOnlyLocalRepo;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isUseOnlyLocalRepo() {
        return useOnlyLocalRepo;
    }
    public String getResult(){
        return result;
    }
    public String getToken(){
        return  token;
    }
    public String getBody(){
        return body;
    }
    public String getMac(){
        return mac;
    }
}
