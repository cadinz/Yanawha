package com.yanawha.osejin.yanawha;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CenterInfoManager{


    public enum Code {

        RESTAURANT("FD6"),
        CAFE("CE7"),
        CONVENIENCESTORE("CS2");
        String code;

        Code(String code) {
            this.code = code;
        }

        private String codeToString() {
            return code;
        }
    }

    public CenterInfoManager() {
    }


    public void getLocationInfo(DataCallback cb, MarkerInfo markerinfo, Code code, String query, int size){
        //call retrofit
            Retrofit retro = new Retrofit.Builder( ).addConverterFactory(GsonConverterFactory.create( ))
                    .baseUrl(RetrofitService.base)
                    .build( );


            RetrofitService api = retro.create(RetrofitService.class);

            Call<JsonObject> call = api.getAddress(query,
                    code.codeToString(), markerinfo.getLng( ) + "", markerinfo.getLat( ) + "",
                    size);
        Callback callback = new Callback<JsonObject>( ) {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        cb.DataCallback(response.body());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                }
            };

            call.enqueue(callback);

    }
}
