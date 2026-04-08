package com.io.dronecontroller.domain.model

/**
 * BLE物理コントローラーのスティック入力値
 * 各値の範囲: -1.0 〜 1.0
 *
 * @param leftX  左スティックX: Yaw（-左回転 / +右回転）
 * @param leftY  左スティックY: Throttle（-下降 / +上昇）
 * @param rightX 右スティックX: Roll（-左 / +右）
 * @param rightY 右スティックY: Pitch（-前進 / +後退）
 */
data class BleControllerState(
    val leftX: Float = 0f,
    val leftY: Float = 0f,
    val rightX: Float = 0f,
    val rightY: Float = 0f
)
