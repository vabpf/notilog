package com.notilog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.zip.GZIPOutputStream

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext) ?: return@withContext Result.failure()
            val credential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Collections.singleton(DriveScopes.DRIVE_APPDATA)
            ).apply {
                selectedAccount = account.account
            }

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential
            ).setApplicationName("Notilog").build()

            // 1. Get Database Path
            val dbFile = applicationContext.getDatabasePath("notilog_db")
            if (!dbFile.exists()) return@withContext Result.failure()

            // 2. Compress Database
            val compressedFile = java.io.File(applicationContext.cacheDir, "notilog_backup.db.gz")
            GZIPOutputStream(FileOutputStream(compressedFile)).use { gzip ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(gzip)
                }
            }

            // 3. Upload to Google Drive (App Data Folder)
            val metadata = File().apply {
                name = "notilog_backup_${System.currentTimeMillis()}.db.gz"
                parents = Collections.singletonList("appDataFolder")
            }
            val content = FileContent("application/x-gzip", compressedFile)

            driveService.files().create(metadata, content).execute()

            // 4. Update last sync time
            val prefs = applicationContext.getSharedPreferences("notilog_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_sync_time", System.currentTimeMillis()).apply()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
