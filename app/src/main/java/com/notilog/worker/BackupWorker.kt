package com.notilog.worker

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.stream.JsonWriter
import com.notilog.data.repository.NotificationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Date
import java.util.zip.GZIPOutputStream

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val backupFolderUri = inputData.getString(KEY_BACKUP_FOLDER_URI) ?: return@withContext Result.failure()
            val treeUri = Uri.parse(backupFolderUri)
            val treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
            val treeDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId)

            val fileName = "notilog_backup.json.gz"
            var backupFileUri: Uri? = null

            try {
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId)
                val projection = arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                )
                applicationContext.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    while (cursor.moveToNext()) {
                        val displayName = cursor.getString(nameColumn)
                        if (displayName == fileName) {
                            val docId = cursor.getString(idColumn)
                            backupFileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to creating a new document if querying fails
            }

            if (backupFileUri == null) {
                backupFileUri = DocumentsContract.createDocument(
                    applicationContext.contentResolver,
                    treeDocumentUri,
                    "application/gzip",
                    fileName
                ) ?: return@withContext Result.retry()
            }
            
            val outputStream = applicationContext.contentResolver.openOutputStream(backupFileUri!!, "w") ?: return@withContext Result.retry()

            GZIPOutputStream(outputStream).bufferedWriter().use { writer ->
                val jsonWriter = JsonWriter(writer)
                jsonWriter.setIndent("  ")
                jsonWriter.beginArray()
                
                val cursor = notificationRepository.getAllNotificationsCursor()
                cursor.use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val sysIdIdx = c.getColumnIndex("systemId")
                    val tagIdx = c.getColumnIndex("tag")
                    val pkgIdx = c.getColumnIndex("packageName")
                    val appIdx = c.getColumnIndex("appName")
                    val titleIdx = c.getColumnIndex("title")
                    val textIdx = c.getColumnIndex("textContent")
                    val timeIdx = c.getColumnIndex("postTime")
                    val dismissedIdx = c.getColumnIndex("isDismissed")
                    val categoryIdx = c.getColumnIndex("category")
                    val deletedIdx = c.getColumnIndex("isDeleted")
                    val deletedAtIdx = c.getColumnIndex("deletedAt")

                    while (c.moveToNext()) {
                        jsonWriter.beginObject()
                        if (idIdx != -1) jsonWriter.name("id").value(c.getLong(idIdx))
                        if (sysIdIdx != -1) jsonWriter.name("systemId").value(c.getInt(sysIdIdx))
                        if (tagIdx != -1) jsonWriter.name("tag").value(c.getString(tagIdx))
                        if (pkgIdx != -1) jsonWriter.name("packageName").value(c.getString(pkgIdx))
                        if (appIdx != -1) jsonWriter.name("appName").value(c.getString(appIdx))
                        if (titleIdx != -1) jsonWriter.name("title").value(c.getString(titleIdx))
                        if (textIdx != -1) jsonWriter.name("textContent").value(c.getString(textIdx))
                        if (timeIdx != -1) jsonWriter.name("postTime").value(c.getLong(timeIdx))
                        if (dismissedIdx != -1) jsonWriter.name("isDismissed").value(c.getInt(dismissedIdx) == 1)
                        if (categoryIdx != -1) jsonWriter.name("category").value(c.getString(categoryIdx))
                        if (deletedIdx != -1) jsonWriter.name("isDeleted").value(c.getInt(deletedIdx) == 1)
                        if (deletedAtIdx != -1 && !c.isNull(deletedAtIdx)) jsonWriter.name("deletedAt").value(c.getLong(deletedAtIdx))
                        jsonWriter.endObject()
                    }
                }
                
                jsonWriter.endArray()
                jsonWriter.close()
            }

            Result.success()
        } catch (error: SecurityException) {
            Result.failure()
        } catch (error: IOException) {
            Result.retry()
        } catch (error: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_BACKUP_FOLDER_URI = "backup_folder_uri"
    }
}
