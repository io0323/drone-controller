package com.io.dronecontroller

import androidx.compose.runtime.Composable
import com.io.dronecontroller.ui.controller.DroneControllerScreen

@Composable
fun App() {
    // UIモック: ドローンコントローラー画面を直接表示
    // 実装後は ConnectionScreen → DroneControllerScreen のナビゲーションに変更する
    DroneControllerScreen()
}
