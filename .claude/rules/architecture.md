# Architecture Rules

## 基本方針
クリーンアーキテクチャ + MVVM の3層構造: `UI → Domain → Data`
各層は **Contract（インターフェース）** を介して依存する。

## プロジェクト構造
```
commonMain
├── ui/
├── domain/
└── data/
androidMain
iosMain
```

## コーディング規約
- **ViewModel**: Contract + 実装 + Mock の3点セット。状態は `MutableStateFlow`（private）+ `StateFlow`（public）。UseCase の Contract に依存する
- **UseCase**: Contract + 実装。単一責任。`operator fun invoke()` で呼び出し。結果は `RunStatus<T>` でラップ
- **Repository**: Contract + 実装。Entity ↔ Domain モデル変換を担当。DataSource の Contract に依存する
- **DataSource**: プラットフォーム固有処理は `expect/actual` + Contract パターン

## ファイル命名規則
- **ViewModel**: `XxxxViewModelContract.kt` / `XxxxViewModel.kt` / `MockXxxxViewModel.kt`
- **UseCase**: `XxxxUseCaseContract.kt` / `XxxxUseCase.kt`
- **Repository**: `XxxxRepositoryContract.kt` / `XxxxRepository.kt`
- **expect/actual**: `Xxxx.common.kt` / `Xxxx.android.kt` / `Xxxx.ios.kt`

## DI（Koin）登録ルール
エントリーポイント: `di/KoinHelper.kt` の `initKoin()`

| モジュール | ファイル | 登録方法 |
|---|---|---|
| `viewModelModule` | `ui/di/ViewModelModule.kt` | `viewModel { XxxxViewModel(get()) }` |
| `useCaseModule` | `domain/di/DomainModule.kt` | `single<Contract> { Impl(get()) }` |
| `repositoryModule` | `data/di/DataModule.common.kt` | `single<Contract> { Impl(get()) }` |
| `dataSourceModule` | `data/di/DataModule.common.kt` | expect/actual で定義 |

## プラットフォーム固有API
**いずれかを変更した場合、もう一方のプラットフォームにも同等の変更が必要。**

- **パターン1（expect/actual）**: androidMain / iosMain に Kotlin 実装
- **パターン2（Contract + Swift）**: commonMain で Contract 定義 → androidMain に Kotlin 実装 → `iosApp/` に Swift 実装

## 実装優先順
1. MAVLink接続・HEARTBEAT受信 ✅
2. メインUI（コントローラー画面）
3. テレメトリ表示（高度・バッテリー・姿勢）
4. 基本コマンド（離陸・着陸・RTL）
5. BLE物理コントローラー連携
6. 地図表示
7. ミッション計画

## 注意事項
- MAVLink接続はForeground Serviceで維持（Android）
- iOS固有処理は将来対応