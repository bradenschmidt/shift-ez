package com.schmidtdesigns.shiftez.network;

import com.schmidtdesigns.shiftez.models.Account;
import com.schmidtdesigns.shiftez.models.ImageUploadUrl;
import com.schmidtdesigns.shiftez.models.PostAccount;
import com.schmidtdesigns.shiftez.models.PostResult;
import com.schmidtdesigns.shiftez.models.Schedule;
import com.schmidtdesigns.shiftez.models.ShareStore;
import com.schmidtdesigns.shiftez.models.Store;

import java.util.Map;

import retrofit.http.DELETE;
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

    // Get account info, includes stores with schedules
    @GET("/api/accounts/{user_id}")
    Account.Response getAccount(@Path("user_id") String user_id);


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
    @GET("/api/stores/schedules/link")
    ImageUploadUrl getImageUploadURL();

    // Send the image with its data
    @Multipart
    @POST("/{imageUrl}")
    PostResult uploadImage(@EncodedPath("imageUrl") String imageUrl,
                           @Part("file") TypedFile image,
                           @PartMap Map<String, String> imageParams);


    ////// STORES ///////
    // Add store to the account
    @POST("/api/accounts/{userId}/stores/add")
    PostResult addNewStore(@Path("userId") String userId,
                           @QueryMap Map<String, String> storeParams);

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
    PostResult removeStore(@Path("userId") String mUserId,
                           @QueryMap Map<String, String> mStoreParams);
}
