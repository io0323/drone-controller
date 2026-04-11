package com.io.dronecontroller.domain.usecase

interface SendManualControlUseCaseContract {
    /**
     * ドローンに手動制御値を送信する（継続的に呼び出す）
     *
     * @param pitch    ピッチ（前後）: -1.0=前進 / +1.0=後退
     * @param roll     ロール（左右）: -1.0=左 / +1.0=右
     * @param throttle スロットル: -1.0=最低出力 / +1.0=最高出力
     * @param yaw      ヨー（回転）: -1.0=左回転 / +1.0=右回転
     */
    operator fun invoke(
        pitch: Float,
        roll: Float,
        throttle: Float,
        yaw: Float,
    )
}
