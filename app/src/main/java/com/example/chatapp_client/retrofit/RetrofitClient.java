package com.example.chatapp_client.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitInterface retrofitInterface;

    public RetrofitClient(){
      String BASE_URL = "http://192.168.100.3:3001";
      Retrofit retrofit = new Retrofit.Builder()
          .baseUrl(BASE_URL)
          .addConverterFactory(GsonConverterFactory.create())
          .build();

      retrofitInterface = retrofit.create(RetrofitInterface.class);
    }

    public RetrofitInterface getServie(){
      return retrofitInterface;
    }
}
