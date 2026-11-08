package com.evilcorp.android.labs

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red

private class UsersDb(context: Context) : SQLiteOpenHelper(context, "evilcorp_users.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT, password TEXT, role TEXT, note TEXT)")
        db.execSQL("INSERT INTO users (username, password, role, note) VALUES ('alice', 'wonderland', 'user', 'Welcome back, Alice.')")
        db.execSQL("INSERT INTO users (username, password, role, note) VALUES ('bob', 'hunter2', 'user', 'Quarterly report is due.')")
        db.execSQL("INSERT INTO users (username, password, role, note) VALUES ('admin', 'X9#kq!vT2@pL', 'admin', '${Flags.SQLI}')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

@Composable
fun SqliLab() {
    val context = LocalContext.current
    val db = remember { UsersDb(context.applicationContext) }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    LabFrame(
        scenario = "The login form checks credentials against a local SQLite database by gluing " +
            "user input straight into the SQL string. Sign in as the admin without knowing the password.",
        remediation = "Use parameterized queries (rawQuery with ? placeholders and selectionArgs, " +
            "or Room). Never authenticate users against a local database; do it server-side.",
    ) {
        LabField(user, { user = it }, "Username")
        LabField(pass, { pass = it }, "Password", secret = true)
        PrimaryButton("Login") {
            // INTENTIONALLY INSECURE: string concatenation -> SQL injection
            query = "SELECT username, role, note FROM users WHERE username='$user' AND password='$pass'"
            result = try {
                db.readableDatabase.rawQuery(query, null).use { c ->
                    if (!c.moveToFirst()) "Invalid credentials." else buildString {
                        do {
                            appendLine("${c.getString(0)} [${c.getString(1)}]: ${c.getString(2)}")
                        } while (c.moveToNext())
                    }
                }
            } catch (e: Exception) {
                "SQL error: ${e.message}"
            }
        }
        if (query.isNotEmpty()) {
            Mono(query, color = Red)
            Mono(result.trim())
        }
    }
}
