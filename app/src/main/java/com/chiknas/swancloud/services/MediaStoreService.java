package com.chiknas.swancloud.services;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * MediaStore service to support functionality around the android mediastore API.
 */
public class MediaStoreService {

    public static final Uri COLLECTION_LOCATION = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);

    private final Context context;

    public MediaStoreService(Context context) {
        this.context = context;
    }

    public Cursor getMediaTakenAfter(LocalDateTime date) {

        String selection = getMediaSelectionTakenAfter(date);

        // Retrieve the files id
        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME
        };

        // Sort by the oldest file first
        String sortOrder = MediaStore.Files.FileColumns.DATE_TAKEN + " ASC";

        return context.getContentResolver().query(
                COLLECTION_LOCATION,
                projection,
                selection,
                null,
                sortOrder
        );
    }

    private String getMediaSelectionTakenAfter(LocalDateTime date) {
        return "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ")"
                + " AND "
                + MediaStore.Files.FileColumns.DATE_TAKEN + " >= " + date.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
