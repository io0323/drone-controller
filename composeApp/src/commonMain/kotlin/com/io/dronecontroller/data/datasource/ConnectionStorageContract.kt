package com.io.dronecontroller.data.datasource

interface ConnectionStorageContract {
    suspend fun saveConnection(address: String, port: Int)

    suspend fun loadLastAddress(): String

    suspend fun loadLastPort(): Int

    suspend fun loadHistory(): List<Pair<String, Int>>
}
