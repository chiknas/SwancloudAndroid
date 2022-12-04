package com.chiknas.swancloud.tasks;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * https://medium.com/swlh/periodic-tasks-with-android-workmanager-c901dd9ba7bc
 */
public class FileSyncPeriodicTask extends Worker {

    public FileSyncPeriodicTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return Result.success();
    }
}
