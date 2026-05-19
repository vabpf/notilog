package com.notilog.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelLogicTest {

    @Test
    fun backupFrequency_fromStoredValue_mapsSafely() {
        assertEquals(BackupFrequency.DAILY, BackupFrequency.fromStoredValue("daily"))
        assertEquals(BackupFrequency.WEEKLY, BackupFrequency.fromStoredValue("weekly"))
        assertEquals(BackupFrequency.DAILY, BackupFrequency.fromStoredValue("unknown"))
        assertEquals(BackupFrequency.DAILY, BackupFrequency.fromStoredValue(null))
    }

    @Test
    fun shouldScheduleAutoBackup_onlyWhenEnabledAndFolderExists() {
        assertFalse(shouldScheduleAutoBackup(enabled = false, folderUri = null))
        assertFalse(shouldScheduleAutoBackup(enabled = true, folderUri = null))
        assertFalse(shouldScheduleAutoBackup(enabled = true, folderUri = ""))
        assertTrue(shouldScheduleAutoBackup(enabled = true, folderUri = "content://tree/backup"))
    }

    @Test
    fun importParser_parsesCsvWithHeader() {
        val csv = """
            systemId,tag,packageName,appName,title,textContent,postTime,isDismissed,category,isDeleted,deletedAt
            7,,com.app.demo,Demo App,Hello,World,1715965200000,false,Social,false,
        """.trimIndent()

        val parsed = NotificationImportParser.parse(csv, nowMillis = 123L)

        assertEquals(1, parsed.size)
        val item = parsed.first()
        assertEquals(7, item.systemId)
        assertNull(item.tag)
        assertEquals("com.app.demo", item.packageName)
        assertEquals("Demo App", item.appName)
        assertEquals("Hello", item.title)
        assertEquals("World", item.textContent)
        assertEquals(1715965200000L, item.postTime)
        assertFalse(item.isDismissed)
        assertEquals("Social", item.category)
    }

    @Test
    fun importParser_parsesJsonArray() {
        val json = """
            [
              {
                "systemId": 9,
                "tag": "chat",
                "packageName": "com.chat.app",
                "appName": "Chat App",
                "title": "Ping",
                "textContent": "New message",
                "postTime": 1715965300000,
                "isDismissed": true,
                "category": "Social",
                "isDeleted": false,
                "deletedAt": null
              }
            ]
        """.trimIndent()

        val parsed = NotificationImportParser.parse(json, nowMillis = 123L)

        assertEquals(1, parsed.size)
        val item = parsed.first()
        assertEquals(9, item.systemId)
        assertEquals("chat", item.tag)
        assertEquals("com.chat.app", item.packageName)
        assertEquals("Chat App", item.appName)
        assertEquals("Ping", item.title)
        assertEquals("New message", item.textContent)
        assertEquals(1715965300000L, item.postTime)
        assertTrue(item.isDismissed)
        assertEquals("Social", item.category)
    }
}
