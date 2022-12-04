package com.chiknas.swancloud.tasks;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
 * If it doesnt work make sure all permissions are given
 * https://medium.com/swlh/periodic-tasks-with-android-workmanager-c901dd9ba7bc
 * https://developer.android.com/training/data-storage/shared/media
 */
public class FileSyncPeriodicTask extends Worker {

    private final ApiService apiService;
    private final ContentResolver resolver;

    public FileSyncPeriodicTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
//        String selection =
//                MediaStore.Video.Media.DATE_TAKEN + " >= " + lastUploadedFileDate +
//                        " OR " +
//                        MediaStore.Images.Media.DATE_TAKEN + " >= " + lastUploadedFileDate;
        String selection = MediaStore.Images.Media.DATE_TAKEN + " >= " + lastUploadedFileDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // Retrieve the files id
        String[] projection = new String[]{
//                MediaStore.Video.Media._ID,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
        };

        // Sort by the oldest file first
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " ASC";

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

    private void uploadFiles(Cursor cursor) throws IOException {
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

        while (cursor.moveToNext()) {

            long id = cursor.getLong(idColumn);
            String name = cursor.getString(nameColumn);

            Uri contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
    }
}
