package com.jian.tangthucac.API;

import com.jian.tangthucac.model.Story;
import com.jian.tangthucac.model.Chapter;
import com.jian.tangthucac.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    // Stories Endpoints
    @GET("stories.json")
    Call<Map<String, Story>> getAllStories();

    @GET("stories/{id}.json")
    Call<Story> getStoryById(@Path("id") String storyId);

    @GET("stories.json?orderBy=\"genre\"&equalTo=\"{genre}\"")
    Call<Map<String, Story>> getStoriesByGenre(@Path("genre") String genre);

    @GET("stories.json?orderBy=\"hot\"&equalTo=true")
    Call<Map<String, Story>> getHotStories();

    @GET("stories/{id}/chapters.json")
    Call<Map<String, Chapter>> getStoryChapters(@Path("id") String storyId);

    // User Library Endpoints
    @GET("Users/{userId}/Library.json")
    Call<Map<String, Object>> getUserLibrary(@Path("userId") String userId);

    @PUT("Users/{userId}/Library/{storyId}.json")
    Call<Void> addToLibrary(@Path("userId") String userId, @Path("storyId") String storyId, @Body Map<String, Object> storyData);

    @DELETE("Users/{userId}/Library/{storyId}.json")
    Call<Void> removeFromLibrary(@Path("userId") String userId, @Path("storyId") String storyId);

    // User Endpoints
    @GET("Users/{userId}.json")
    Call<User> getUserProfile(@Path("userId") String userId);

    @PATCH("Users/{userId}.json")
    Call<User> updateUserProfile(@Path("userId") String userId, @Body Map<String, Object> updates);
}