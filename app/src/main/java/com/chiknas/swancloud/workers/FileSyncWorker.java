package com.chiknas.swancloud.workers;


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
import com.chiknas.swancloud.api.services.user.CurrentUserDetails;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
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
 *
 * Call this worker using one of the following methods:
 * Periodic work:
 * PeriodicWorkRequest periodicFileSyncWorkRequest = new PeriodicWorkRequest.Builder(FileSyncPeriodicTask.class, 1, TimeUnit.HOURS).setConstraints(constraints).build();
 * WorkManager.getInstance(context).enqueueUniquePeriodicWork("FileSyncPeriodicTask", ExistingPeriodicWorkPolicy.REPLACE, periodicFileSyncWorkRequest);
 *
 * One time work:
 * OneTimeWorkRequest periodicFileSyncWorkRequest = new OneTimeWorkRequest.Builder(FileSyncPeriodicTask.class).build();
 * WorkManager.getInstance(context).enqueueUniqueWork("FileSyncTask2", ExistingWorkPolicy.REPLACE, periodicFileSyncWorkRequest);
 */
public class FileSyncWorker extends Worker {

    private final ApiService apiService;
    private final ContentResolver resolver;

    public FileSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.apiService = new ApiService(getApplicationContext());
        this.resolver = getApplicationContext().getContentResolver();
    }

    @NonNull
    @Override
    public Result doWork() {

        // Stop worker we can not find the current user details from the server
        Optional<CurrentUserDetails> currentUserDetails = getCurrentUserDetails();
        if (!currentUserDetails.isPresent()) {
            return Result.failure();
        }

        // Select only files that are created after
        LocalDate lastUploadedFileDate = currentUserDetails.get().getLastUploadedFileDate();
        String selection = getLatestMedia(lastUploadedFileDate);

        // Retrieve the files id
        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME
        };

        // Sort by the oldest file first
        String sortOrder = MediaStore.Files.FileColumns.DATE_TAKEN + " ASC";

        try (Cursor cursor = getApplicationContext().getContentResolver().query(
                getCollectionLocation(),
                projection,
                selection,
                null,
                sortOrder
        )) {
            uploadFiles(cursor);
        } catch (IOException e) {
            return Result.failure();
        }


        return Result.success();
    }

    private String getLatestMedia(LocalDate lastUploadedFileDate){
        return "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ")"
                + " AND "
                + MediaStore.Files.FileColumns.DATE_TAKEN + " >= " + lastUploadedFileDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void uploadFiles(Cursor cursor) throws IOException {
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
        int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);

        while (cursor.moveToNext()) {

            long id = cursor.getLong(idColumn);
            String name = cursor.getString(nameColumn);

            Uri contentUri = ContentUris.withAppendedId(getCollectionLocation(), id);

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

    private Uri getCollectionLocation() {
        return MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
    }
}
