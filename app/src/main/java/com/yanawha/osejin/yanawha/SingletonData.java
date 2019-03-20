package com.yanawha.osejin.yanawha;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public class SingletonData {

    private JsonObject result = new JsonObject();
    private String str = new String();
    private ArrayList<String> str22 = new ArrayList<>();
    private static class LazyHolder {
        static final SingletonData INSTANCE = new SingletonData();
    }

    public static SingletonData getInstance() {
        return LazyHolder.INSTANCE;
    }


    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public void setStr22(String str22) {
        this.str22.add(str22);
    }

    public ArrayList<String> getStr22() {
        return str22;
    }

    public void setresult(JsonObject results) {
        this.result = results;
    }

    public JsonObject getResult() {
        return result;
    }
}
