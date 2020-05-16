package com.OpenNAC.openauth.Services;

public class Post {
    // Variable names have to be equal to the JSON properties
    /// for POST method auth

    private String username;
    private String password;
    private boolean useOnlyLocalRepo;
    private String result;
    private String token;
    private String id;
    private String mail;
    public Post(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getResult(){
        return result;
    }

    public String getToken(){
        return  token;
    }

    public String getMail() { return mail; }

    public String getId() {
        return id;
    }



}
