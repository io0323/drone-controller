package com.io.dronecontroller.data.datasource

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.connectionDataStore by preferencesDataStore("connection")

class ConnectionStorageImpl(
    private val context: Context
) : ConnectionStorageContract {
    private val ADDRESS = stringPreferencesKey("address")
    private val PORT = intPreferencesKey("port")
    private val HISTORY = stringPreferencesKey("history")

    override suspend fun saveConnection(address: String, port: Int) {
        context.connectionDataStore.edit { prefs ->
            prefs[ADDRESS] = address
            prefs[PORT] = port
            val prev = parseHistory(prefs[HISTORY]).filter { it.first != address || it.second != port }
            prefs[HISTORY] =
                (listOf(address to port) + prev)
                    .take(5)
                    .joinToString("|") { "${it.first}:${it.second}" }
        }
    }

    override suspend fun loadLastAddress(): String =
        context.connectionDataStore.data.first()[ADDRESS] ?: "192.168.3.11"

    override suspend fun loadLastPort(): Int =
        context.connectionDataStore.data.first()[PORT] ?: 50051

    override suspend fun loadHistory(): List<Pair<String, Int>> =
        parseHistory(context.connectionDataStore.data.first()[HISTORY])

    private fun parseHistory(raw: String?): List<Pair<String, Int>> =
        (raw ?: "").split("|").filter { it.isNotBlank() }.mapNotNull { entry ->
            val i = entry.lastIndexOf(":")
            if (i < 0) null else entry.substring(0, i) to (entry.substring(i + 1).toIntOrNull() ?: 50051)
        }
}
