package com.yanawha.osejin.yanawha;

import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface RetrofitService {

    String base = "https://dapi.kakao.com/";
    @Headers("Authorization: KakaoAK "+BuildConfig.KAKAO_API_KEY)


    @GET("/v2/local/search/keyword.json")
    Call<JsonObject> getAddress(@Query("query") String query, @Query("category_group_code") String code, @Query("x") String lng, @Query("y") String lat, @Query("size") int size);
}