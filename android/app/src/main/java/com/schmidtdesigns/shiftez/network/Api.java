package com.schmidtdesigns.shiftez.network;

import com.schmidtdesigns.shiftez.models.Account;
import com.schmidtdesigns.shiftez.models.ImageUploadUrl;
import com.schmidtdesigns.shiftez.models.PostAccount;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.ScheduleResponse;
import com.schmidtdesigns.shiftez.models.ShareStore;
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

    ///// ACCOUNTS /////
    @POST("/api/accounts/add")
    PostAccount addAccount(@QueryMap Map<String, String> accountParams);

    @GET("/api/accounts/{user_id}")
    Account.Response getAccount(@Path("user_id") String user_id);


    ///// Schedules /////
    // Get all the accounts schedules
    @GET("/api/accounts/{userId}/schedules/all")
    ScheduleResponse getSchedules(@Path("userId") String userId,
                                  @Query("reverse") boolean reverse);

    // Get the accounts schedules by year
    @GET("/api/accounts/{userId}/schedules/year/{year}")
    ScheduleResponse getSchedules(@Path("userId") String userId,
                                  @Path("year") int year,
                                  @Query("reverse") boolean reverse);

    // Get an image upload link
    @GET("/api/stores/schedules/link")
    ImageUploadUrl getImageUploadURL();

    // Send the image with its data
    @Multipart
    @POST("/{imageUrl}")
    PostResult uploadImage(@EncodedPath("imageUrl") String imageUrl,
                           @Part("file") TypedFile image,
                           @PartMap Map<String, String> imageParams);


    ////// STORES ///////
    @POST("/api/stores/add")
    PostResult addNewStore(@QueryMap Map<String, String> storeParams);

    @GET("api/accounts/{userId}/stores/all")
    StoreResponse getAccountStores(@Path("user_id") String user_id);

    @POST("/api/stores/share")
    ShareStore shareStore(@QueryMap Map<String, String> storeParams);

    @POST("/api/stores/join")
    PostResult joinStore(@QueryMap Map<String, String> storeParams);
}
