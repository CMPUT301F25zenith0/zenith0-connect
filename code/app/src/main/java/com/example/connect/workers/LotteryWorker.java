package com.example.connect.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.connect.utils.LotteryScheduler;

/**
 * Background worker that periodically checks for lottery draws
 * Runs every 15 minutes to find events needing automatic lottery
 *
 * @author Zenith Team
 * @version 1.0
 */
public class LotteryWorker extends Worker {

    private static final String TAG = "LotteryWorker";

    public LotteryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "========================================");
        Log.d(TAG, "LotteryWorker execution started");
        Log.d(TAG, "========================================");

        try {
            // Create scheduler and check for lotteries
            LotteryScheduler scheduler = new LotteryScheduler();
            scheduler.checkAndPerformLotteries();

            Log.d(TAG, "LotteryWorker completed successfully");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error in LotteryWorker", e);
            // Retry on failure
            return Result.retry();
        }
    }
}