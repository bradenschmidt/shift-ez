package com.schmidtdesigns.shiftez.network;

import com.schmidtdesigns.shiftez.models.ImageUploadUrl;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.ScheduleResponse;
import com.schmidtdesigns.shiftez.models.StoreResponse;

import java.util.Map;

import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import retrofit.mime.TypedFile;

/**
 * Defines methods of the API.
 * Used by retrofit to define request types.
 * Allows java calls to the api with one liners.
 * <p/>
 * Created by braden on 15-06-08.
 */
public interface Api {

    ///// Schedules /////
    // Get all the schedules with the given params
    @GET("/api/schedules/all")
    ScheduleResponse getSchedules(@QueryMap Map<String, String> scheduleParams);

    // Get the schedules by year with the given params
    @GET("/api/schedules/year/{year}")
    ScheduleResponse getSchedules(@Path("year") int year, @QueryMap Map<String, String> scheduleParams);

    // Get an image upload link
    @GET("/api/upload/link")
    ImageUploadUrl getImageUploadURL();

    @POST("/api/stores/add")
    PostResult addNewStore(@QueryMap Map<String, String> storeParams);

    @GET("/api/stores/all")
    StoreResponse getStores(@Query("user_id") String user_id);

    // Send the image with its data
    @Multipart
    @POST("/{imageUrl}")
    PostResult uploadImage(@EncodedPath("imageUrl") String imageUrl, @Part("file") TypedFile image, @PartMap Map<String, String> imageParams);

}
