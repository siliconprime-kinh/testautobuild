package com.dropininc.model;

import android.text.TextUtils;

import retrofit2.Response;

public class RetrofitErrorModel {
    public String code;
    public String message;
    public String log;

    public RetrofitErrorModel() {
    }

    public RetrofitErrorModel(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public RetrofitErrorModel(String code, String message, String log) {
        this.code = code;
        this.message = message;
        this.log = log;
    }

    public static RetrofitErrorModel defaultError() {
        return new RetrofitErrorModel("UNKNOWN", "Something went wrong");
    }
    public static RetrofitErrorModel defaultError(Throwable throwable) {
        String log = throwable != null ? ("Throwable:" + throwable.getMessage()):"Something went wrong";
        String log1 = log.toLowerCase();
        if(!TextUtils.isEmpty(log1) && log1.contains("unable to resolve host")){
            return new RetrofitErrorModel("UNKNOWN", "Please check Network connection", log);
        }else{
            return new RetrofitErrorModel("UNKNOWN", "Something went wrong", log);
        }
    }

    public static RetrofitErrorModel defaultError(Response response) {
        String log = response != null ? ("Response:" +response.message() + ".code=" + response.code()):"Something went wrong";
        return new RetrofitErrorModel("UNKNOWN", "Something went wrong", log);
    }

}
