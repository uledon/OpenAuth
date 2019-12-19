package com.OpenNAC.openauth.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserService {
   @Headers({"accept: application/json","Content-Type: application/json"})
   @POST("auth")
   Call<Post>createPost(@Body Post post);
   @FormUrlEncoded
   @POST("auth")
   Call<Post>createPost(
           @Field("username") String username,
           @Field("password") String password,
           @Field("useOnlyLocalRepo") boolean useOnlyLocalRepo
   );
}