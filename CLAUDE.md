# drone-controller

## 概要
MAVLink + UDP通信でPX4 SITLシミュレーターに接続し、
実機なしでもドローン制御アプリとして動作することを実証する。
KMP（Kotlin Multiplatform）+ Compose Multiplatform を使用。Android / iOS 対応。

## 技術スタック
- **Kotlin**: 最新安定版
- **Compose Multiplatform**: 最新安定版
- **Koin**: DI
- **Ktor**: HTTP / UDP通信補助
- **io.mavsdk**: MAVLink通信
- **Android SDK**: minSdk 26, targetSdk 最新安定版

## 実行コマンド
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:testDebugUnitTest
./gradlew ktlintCheck
./gradlew ktlintFormat
```

## 実装優先順
1. MAVLink接続・HEARTBEAT受信
2. テレメトリ表示（高度・バッテリー・姿勢）
3. 基本コマンド（離陸・着陸・RTL）
4. 地図表示
5. ミッション計画

## 注意事項
- MAVLink接続はForeground Serviceで維持（Android）
- エミュレーターからSITL接続: `10.0.2.2:14550`
- iOS固有処理は将来対応

## 詳細ルール
@.claude/rules/architecture.md
@.claude/rules/mavlink.md