package com.example.currentlocation;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface DataJsonApi {

    @Multipart
    @POST("upload_api.php")
    Call<DataModel> getDataModel(@Part MultipartBody.Part image,
                                 @Part("nama")String name,
                                 @Part("latitude")Double latitude,
                                 @Part("longitude")Double longitude);

}
