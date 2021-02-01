package com.example.chatapp_client.retrofit;

import com.example.chatapp_client.utils.*;
import retrofit2.Call;
import retrofit2.http.*;

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

  @POST("/searchuser")
  Call<SearchResult> executeSearchUser(@Body HashMap<String, String> map);

  @POST("/searchconversation")
  Call<FindedConverName> executeFindConvName(@Body HashMap<String, String> map);

  @Headers({ "Content-Type: application/json;charset=UTF-8"})
  @POST("/checkcontacts")
  Call<CheckContacts> executeCheckContacts(@Header("Authorization") String authToken, @Body HashMap<String, String> map);

  @Headers({ "Content-Type: application/json;charset=UTF-8"})
  @POST("/addusertocontactlist")
  Call<Void> executeAddUserToContactList(@Header("Authorization") String authToken, @Body HashMap<String, String> saveUserToDb);

  @Headers({ "Content-Type: application/json;charset=UTF-8"})
  @POST("/deleteuserfromcontactlist")
  Call<Void> executeDeleteUserFromContactList(@Header("Authorization") String authToken, @Body HashMap<String, String> deleteUserFromDb);

  @Headers({ "Content-Type: application/json;charset=UTF-8"})
  @POST("/checkmessages")
  Call<CheckMessages> executeCheckMessages(@Header("Authorization") String authToken, @Body HashMap<String, String> mapCheckMessages);
}
