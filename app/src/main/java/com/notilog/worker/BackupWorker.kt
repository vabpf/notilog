package com.notilog.worker

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notilog.data.local.NotificationDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationDao: NotificationDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val backupFolderUri = inputData.getString(KEY_BACKUP_FOLDER_URI) ?: return@withContext Result.failure()
            val treeUri = Uri.parse(backupFolderUri)
            val treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
            val treeDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId)

            val notifications = notificationDao.getAllNotifications().first()
            val fileName = "notilog_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
            val backupFileUri = DocumentsContract.createDocument(
                applicationContext.contentResolver,
                treeDocumentUri,
                "text/csv",
                fileName
            ) ?: return@withContext Result.retry()
            val outputStream = applicationContext.contentResolver.openOutputStream(backupFileUri) ?: return@withContext Result.retry()

            outputStream.bufferedWriter().use { writer ->
                writer.write("systemId,tag,packageName,appName,title,textContent,postTime,isDismissed,category,isDeleted,deletedAt\n")
                notifications.forEach { notification ->
                    writer.write(
                        listOf(
                            notification.systemId.toString(),
                            escapeCsv(notification.tag),
                            escapeCsv(notification.packageName),
                            escapeCsv(notification.appName),
                            escapeCsv(notification.title),
                            escapeCsv(notification.textContent),
                            notification.postTime.toString(),
                            notification.isDismissed.toString(),
                            escapeCsv(notification.category),
                            notification.isDeleted.toString(),
                            notification.deletedAt?.toString().orEmpty()
                        ).joinToString(",")
                    )
                    writer.write("\n")
                }
            }

            Result.success()
        } catch (error: SecurityException) {
            Result.failure()
        } catch (error: IOException) {
            Result.retry()
        } catch (error: IllegalStateException) {
            Result.retry()
        } catch (error: IllegalArgumentException) {
            Result.failure()
        } catch (error: Exception) {
            Result.retry()
        }
    }

    private fun escapeCsv(value: String?): String {
        if (value == null) return ""
        return "\"${value.replace("\"", "\"\"")}\""
    }

    companion object {
        const val KEY_BACKUP_FOLDER_URI = "backup_folder_uri"
    }
}
