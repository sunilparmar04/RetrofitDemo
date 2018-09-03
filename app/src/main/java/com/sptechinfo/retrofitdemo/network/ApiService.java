package com.sptechinfo.retrofitdemo.network;
/**
 * Created by sunil on 03/09/18.
 */

import com.sptechinfo.retrofitdemo.network.model.Note;
import com.sptechinfo.retrofitdemo.network.model.User;

import java.util.List;
import java.util.Map;


import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;


public interface ApiService {
    // Register new user
    @FormUrlEncoded
    @POST("notes/user/register")
    Call<User> register(@FieldMap(encoded = true) Map<String, String> options);

    @GET("notes/all")
    Single<List<Note>> fetchAllNotes();


    @FormUrlEncoded
    @POST("notes/new")
    Single<Note> createNote(@Field("note") String note);

  /*  @FormUrlEncoded
    @POST("notes/new")
    Call<List<Note>>  createNote(@Field("note") String note);*/

    @FormUrlEncoded
    @PUT("notes/{id}")
    Completable updateNote(@Path("id") int noteId, @Field("note") String note);


    // Delete note
    @DELETE("notes/{id}")
    Completable deleteNote(@Path("id") int noteId);


}
