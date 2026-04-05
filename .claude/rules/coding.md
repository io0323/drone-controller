# Coding Rules

## 基本方針
- 小さな差分を優先。依頼されていないリファクタリングは禁止
- 変更したロジックには必ずテストを追加/修正
- 説明には必ず具体例を含める

## Kotlin
- 可能な限り `val` を使う（`var` は最小限）
- `null` より `sealed class` / `Result` / `RunStatus<T>` でエラー表現
- 拡張関数を活用してコードを簡潔に保つ

## Compose
- Composable関数は単一責任
- UIロジックをComposableに書かない（ViewModelに委譲）
- Previewは必ず追加する

## テスト
- ViewModelのテストは必ず書く
- MockViewModelを活用してUIテストを簡潔に保つ

## ktlint 設定
無効化ルール（`.editorconfig`）:
- `trailing-comma`
- `function-signature`
- `parameter-list-wrapping`
- `expression-body-syntax`
- `backing-property` / `property-naming`（`_xxx` StateFlow 許可）
- `filename`（`Xxxx.android.kt` 形式許可）
- Composable 関数名の除外

ktlint 対象外:
- テストコード
- ビルド出力
- Gradle ファイル