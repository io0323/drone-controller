# MAVLink Rules

## ライブラリ
- io.mavsdk（Kotlin向け公式SDK）

## 接続
- プロトコル: UDP
- ポート: 14550（PX4 SITLデフォルト）
- エミュレーターから接続: `10.0.2.2:14550`

## 主要メッセージ
| メッセージ | 用途 |
|---|---|
| HEARTBEAT | 接続死活監視（1秒ごと） |
| GLOBAL_POSITION_INT | 緯度・経度・高度 |
| BATTERY_STATUS | バッテリー残量 |
| ATTITUDE | ロール・ピッチ・ヨー |
| SYS_STATUS | システム全体の状態 |

## 基本コマンド
| コマンド | 用途 |
|---|---|
| MAV_CMD_NAV_TAKEOFF | 離陸 |
| MAV_CMD_NAV_LAND | 着陸 |
| MAV_CMD_NAV_RETURN_TO_LAUNCH | RTL |

## 設計方針
- MAVLink処理はRepositoryに閉じ込める
- テレメトリはFlowで流してUIに届ける
- HEARTBEATが途絶えたら接続断として扱う
- コマンド送信失敗時は最大3回リトライ

## 実装パターン
```kotlin
val drone = System("localhost", 50051)
drone.telemetry.position
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe { position -> /* UIに反映 */ }
```