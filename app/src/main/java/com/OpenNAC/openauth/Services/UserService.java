package com.OpenNAC.openauth.Services;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserService {
   //@Headers({"accept: application/json","Content-Type: application/json"})

//   @POST("mobileauth")
//   Call<Post>createPost(@Body Post post);

   @Multipart
   @POST("mobileauth")
   Call<Post> createPost(
           @Header("User-Agent") String useragent,
           @Part("username") RequestBody username,
           @Part("password") RequestBody password
   );
//   @GET("statususerdev")
//   Call<List<Post>> getPosts(
//           @Header("X-Opennac-Token") String token,
//           @Header("X-Opennac-Username") String user,
//           @Query("filters") String filter,
//           @Query("start-index") int startIndex,
//           @Query("max-result") int maxQuery,
//           @Query("sort") String sort
//   );

    @GET("configuration?id=mobileApp")
    Call<User> getConfiguration(
            @Header("User-Agent") String useragent
    );
    @Multipart
    @POST("mobile-2fa-notification")
    Call<User>mobile_notification(
            @Header("X-Opennac-Token") String token,
            @Header("X-Opennac-Username") String user,
            @Header("Cookie") String cookie,
            @Header("User-Agent") String useragent,
            @Part("action") RequestBody action,
            @Part("challengeId") RequestBody challengeId,
            @Part("response") RequestBody response,
            @Part("location") RequestBody location
    );
    @Multipart
    @POST("mobile-2fa-notification")
    Call<User>send_notification(
            @Header("X-Opennac-Token") String token,
            @Header("X-Opennac-Username") String user,
            @Header("Cookie") String cookie,
            @Header("User-Agent") String useragent,
            @Part("action") RequestBody action,
            @Part("deviceId") RequestBody deviceId,
            @Part("apiKey") RequestBody apiKey,
            @Part("fcmt") RequestBody fcmt,
            @Part("ttl") RequestBody ttl,
            @Part("title") RequestBody title,
            @Part("message") RequestBody message
    );
    @Multipart
    @POST("mobilelogin")
    Call<User>mobilelogin(
            @Header("Cookie") String cookie,
            @Header("User-Agent") String useragent,
            @Part("mac") RequestBody mac,
            @Part("factor") RequestBody factor,
            @Part("fcmt") RequestBody fcmt,
            @Part("vendor") RequestBody vendor,
            @Part("os")RequestBody os ,
            @Part("version") RequestBody version,
            @Part("ip_addr") RequestBody ip_addr,
            @Part("model") RequestBody model,
            @Part("security_patch") RequestBody security_patch,
            @Part("hostname") RequestBody hostname,
            @Part("ssid") RequestBody ssid,
            @Part("timestamp") RequestBody timestamp,
            @Part("location") RequestBody location
    );
    @DELETE("mobileauth")
    Call<Void>logout(
            //@Header("Cookie") String cookie,
            @Header("User-Agent") String useragent
            );
//    @POST ("mobilelogin")
//    Call<User>mobilelogin(
//        @Body User user
//    );


}