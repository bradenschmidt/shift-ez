package com.schmidtdesigns.shiftez.network;

import com.schmidtdesigns.shiftez.models.ImageUploadUrl;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.ScheduleResponse;

import java.util.Map;

import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.PartMap;
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
    // Get the schedules with the given params
    @GET("/get")
    ScheduleResponse getSchedules(@QueryMap Map<String, String> scheduleParams);

    // Get an image upload link
    @GET("/upload/link")
    ImageUploadUrl getImageUploadURL();

    // Send the image with its data
    @Multipart
    @POST("/{imageUrl}")
    PostResult uploadImage(@EncodedPath("imageUrl") String imageUrl, @Part("file") TypedFile image, @PartMap Map<String, String> imageParams);

    //@PUT("/users/{userid}/sections")
    //Boolean addUserToSection(@Path("userid") String userId, @QueryMap Map<String, String> userSectionParams);

}
