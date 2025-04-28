
package com.jian.tangthucac.API;

import com.jian.tangthucac.model.Story;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FirebaseAPI {
    @GET("stories.json")
    Call<Map<String, Story>> getStories();
}
