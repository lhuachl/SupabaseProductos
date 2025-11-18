package com.example.supabaseproductos.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.supabaseproductos.data.repository.Repository
import com.example.supabaseproductos.util.SoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = Repository(context)
    private val soundManager = SoundManager()

    override suspend fun doWork(): Result {
        return try {
            if (repository.isConnected()) {
                val syncResult = repository.syncData()
                if (syncResult.isSuccess) {
                    soundManager.playSyncSound()
                    Result.success()
                } else {
                    Result.retry()
                }
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        } finally {
            soundManager.release()
        }
    }
}
