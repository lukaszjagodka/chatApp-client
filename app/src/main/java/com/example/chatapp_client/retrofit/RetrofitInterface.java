package com.example.chatapp_client.retrofit;

import com.example.chatapp_client.utils.LoginResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import java.util.HashMap;

public interface RetrofitInterface {
  @POST("/login")
  Call<LoginResult> executeLogin(@Body HashMap<String, String> map);

  @POST("/signup")
  Call<Void> executeSignup(@Body HashMap<String, String> map);

  @POST("/rememberpassword")
  Call<Void> executeRememberPassword(@Body HashMap<String, String> map);

  @Headers({ "Content-Type: application/json;charset=UTF-8"})
  @POST("/passwordchange")
  Call<Void> executePasswordChange(@Header("Authorization") String authToken, @Body HashMap<String, String> map);
}
