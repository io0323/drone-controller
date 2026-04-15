package com.io.dronecontroller.data.datasource

class ConnectionStorageStub : ConnectionStorageContract {
    private var lastAddress = "10.0.2.2"
    private var lastPort = 50051
    private val history = mutableListOf<Pair<String, Int>>()

    override suspend fun saveConnection(address: String, port: Int) {
        lastAddress = address
        lastPort = port
        history.removeAll { it.first == address && it.second == port }
        history.add(0, address to port)
        if (history.size > 5) history.removeAt(history.lastIndex)
    }

    override suspend fun loadLastAddress() = lastAddress

    override suspend fun loadLastPort() = lastPort

    override suspend fun loadHistory(): List<Pair<String, Int>> = history.toList()
}
