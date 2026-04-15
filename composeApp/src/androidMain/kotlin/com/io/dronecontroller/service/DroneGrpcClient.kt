package com.io.dronecontroller.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DroneGrpcClient {
    fun connect(): Flow<String> =
        flow {
            try {
                // 本来のgRPC接続
                emit("接続試行中...")

                // ダミー（サーバ無しでも動く）
                delay(1000)
                emit("オフラインモード")
            } catch (e: Exception) {
                emit("接続失敗")
            }
        }
}
