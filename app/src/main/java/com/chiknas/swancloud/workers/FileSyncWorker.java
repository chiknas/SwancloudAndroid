package com.chiknas.swancloud.workers;


import static com.chiknas.swancloud.services.MediaStoreService.COLLECTION_LOCATION;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.chiknas.swancloud.api.ApiService;
import com.chiknas.swancloud.api.apiservices.user.CurrentUserDetails;
import com.chiknas.swancloud.services.MediaStoreService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * WorkManager worker responsible to upload the latest files into the system.
 * Retrieves the last updated date from the server and then uses that to scan the
 * phone for any fotos/videos that were taken after that date (meaning they have not been
 * uploaded yet). Then proceeds to upload each file one by one.
 * This is preferred instead of a batch upload, in case something goes wrong, we will be able
 * to resume upload from where we left off by just getting the new lastUploadedFileDate from the server.
 * <p>
 * Call this worker using one of the following methods:
 * Periodic work:
 * PeriodicWorkRequest periodicFileSyncWorkRequest = new PeriodicWorkRequest.Builder(FileSyncWorker.class, 1, TimeUnit.HOURS).setConstraints(constraints).build();
 * WorkManager.getInstance(context).enqueueUniquePeriodicWork("FileSyncWorker", ExistingPeriodicWorkPolicy.REPLACE, periodicFileSyncWorkRequest);
 * <p>
 * One time work:
 * OneTimeWorkRequest periodicFileSyncWorkRequest = new OneTimeWorkRequest.Builder(FileSyncWorker.class).build();
 * WorkManager.getInstance(context).enqueueUniqueWork("FileSyncWorker", ExistingWorkPolicy.REPLACE, periodicFileSyncWorkRequest);
 */
public class FileSyncWorker extends Worker {

    private final ApiService apiService;
    private final ContentResolver resolver;
    private final MediaStoreService mediaStoreService;

    public FileSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.apiService = new ApiService(getApplicationContext());
        this.resolver = getApplicationContext().getContentResolver();
        this.mediaStoreService = new MediaStoreService(getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {

        // Stop worker we can not find the current user details from the server
        Optional<CurrentUserDetails> currentUserDetails = getCurrentUserDetails();
        if (!currentUserDetails.isPresent()) {
            return Result.failure();
        }

        // Select only files that are created after the last uploaded file
        LocalDateTime lastUploadedFileDate = currentUserDetails.get().getLastUploadedFileDate();

        try (Cursor cursor = mediaStoreService.getMediaTakenAfter(lastUploadedFileDate)) {
            uploadFiles(cursor);
        } catch (IOException e) {
            return Result.failure();
        }


        return Result.success();
    }

    private void uploadFiles(Cursor cursor) throws IOException {
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);

        while (cursor.moveToNext()) {

            long id = cursor.getLong(idColumn);
            String name = cursor.getString(nameColumn);

            Uri contentUri = ContentUris.withAppendedId(COLLECTION_LOCATION, id);

            uploadFile(name, contentUri);
        }
    }

    private void uploadFile(String name, Uri contentUri) throws IOException {
        try (InputStream inputStream = resolver.openInputStream(contentUri)) {
            byte[] targetArray = new byte[inputStream.available()];
            inputStream.read(targetArray);

            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), targetArray);

            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("files", name, requestFile);

            apiService.getFilesApi().uploadFiles(Collections.singletonList(body)).execute();
        }
    }

    private Optional<CurrentUserDetails> getCurrentUserDetails() {
        try {
            Response<CurrentUserDetails> response = new ApiService(getApplicationContext()).getUserApi().currentUserDetails().execute();
            if (!response.isSuccessful()) {
                return Optional.empty();
            }

            return Optional.ofNullable(response.body());
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
