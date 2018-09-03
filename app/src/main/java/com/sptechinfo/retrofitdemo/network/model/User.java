package com.sptechinfo.retrofitdemo.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sunil on 03/09/18.
 */
public class User extends BaseResponse {

    @SerializedName("api_key")
    String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}
