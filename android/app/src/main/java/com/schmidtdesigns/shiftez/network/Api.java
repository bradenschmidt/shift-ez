package com.schmidtdesigns.shiftez.network;

import com.schmidtdesigns.shiftez.models.Account;
import com.schmidtdesigns.shiftez.models.ImageUploadUrl;
import com.schmidtdesigns.shiftez.models.PostAccount;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.Schedule;
import com.schmidtdesigns.shiftez.models.ShareStore;
import com.schmidtdesigns.shiftez.models.Store;

import java.util.Map;

import retrofit.http.*;
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
    @POST("/api/accounts/{userId/add")
    PostAccount addAccount(@Path("userId") String userId,
                           @QueryMap Map<String, String> accountParams);

    // Get account info, includes stores with schedules
    @GET("/api/accounts/{userId}")
    Account.Response getAccount(@Path("userId") String userId);


    ///// Schedules /////
    // Get all the accounts schedules
    @GET("/api/accounts/{userId}/schedules/all")
    Schedule.Response getSchedules(@Path("userId") String userId,
                                  @Query("reverse") boolean reverse);

    // Get the accounts schedules by year
    @GET("/api/accounts/{userId}/schedules/year/{year}")
    Schedule.Response getSchedules(@Path("userId") String userId,
                                  @Path("year") int year,
                                  @Query("reverse") boolean reverse);

    // Get an image upload link
    @GET("/api/accounts/{userId}/schedules/link")
    ImageUploadUrl getImageUploadURL(@Path("userId") String userId);

    // Send the image with its data
    @Multipart
    @POST("/{imageUrl}")
    PostResult uploadImage(@EncodedPath("imageUrl") String imageUrl,
                           @Part("file") TypedFile image,
                           @PartMap Map<String, String> imageParams);


    ////// STORES ///////
    // Add store to the account
    @FormUrlEncoded
    @POST("/api/accounts/{userId}/stores/add")
    PostResult addNewStore(@Path("userId") String userId,
                           @FieldMap Map<String, String> storeParams);

    // Get all the stores of the user
    @GET("/api/accounts/{userId}/stores/all")
    Store.Response getAccountStores(@Path("userId") String userId);

    // Share store to return a key for sending to other users
    @POST("/api/accounts/{userId}/stores/share")
    ShareStore shareStore(@Path("userId") String userId,
                          @QueryMap Map<String, String> storeParams);

    // Join store with given key
    @POST("/api/accounts/{userId}/stores/join")
    PostResult joinStore(@Path("userId") String userId,
                         @Query("key") String key);

    // Remove store from account
    @DELETE("/api/accounts/{userId}/stores/remove")
    PostResult removeStore(@Path("userId") String userId,
                           @QueryMap Map<String, String> mStoreParams);
}
