package com.evilcorp.android.labs

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.ui.Mono

/** INTENTIONALLY INSECURE: exported, no read permission, no row-level filtering. */
class NotesProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?,
    ): Cursor = MatrixCursor(arrayOf("_id", "owner", "visibility", "body")).apply {
        addRow(arrayOf<Any>(1, "alice", "public", "Team lunch on Friday."))
        addRow(arrayOf<Any>(2, "bob", "public", "Remember to rotate the VPN cert."))
        addRow(arrayOf<Any>(3, "admin", "private", "Incident 7 root cause: ${Flags.PROVIDER}"))
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.evilcorp.note"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
}

@Composable
fun ProviderLab() {
    val context = LocalContext.current
    val visible = remember {
        buildString {
            context.contentResolver.query(Uri.parse("content://com.evilcorp.android.notes/notes"), null, null, null, null)
                ?.use { c ->
                    while (c.moveToNext()) {
                        // The app UI hides private notes... the provider does not.
                        if (c.getString(2) == "public") appendLine("#${c.getInt(0)} ${c.getString(1)}: ${c.getString(3)}")
                    }
                }
        }.trim()
    }

    LabFrame(
        scenario = "Team notes are served by a ContentProvider. The app only displays public notes, " +
            "but the provider is exported and filters nothing. Read the private note from outside the app.",
        remediation = "Set android:exported=\"false\" unless other apps truly need access. Otherwise " +
            "protect it with a signature-level permission, enforce authorization per row in query(), " +
            "and use grantUriPermissions for one-off sharing.",
    ) {
        Mono(visible)
    }
}
