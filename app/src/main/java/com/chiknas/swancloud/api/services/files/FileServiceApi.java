package com.chiknas.swancloud.api.services.files;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FileServiceApi {

    @GET("/api/files")
    Call<List<FileMetadata>> files(
            @Query("limit") int limit,
            @Query("offset") int offset
    );

}
