package com.chiknas.swancloud.api.apiservices.files;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface FileServiceApi {

    @GET("/api/files")
    Call<List<FileMetadata>> files(
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @Multipart
    @POST("/api/upload")
    Call<Void> uploadFiles(@Part List<MultipartBody.Part> files);

}
