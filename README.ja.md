<div align="center">

# AIChat

AI Chat Reimagined // AIキャラクターとチャット

[简体中文](README.md) | [English](README.en.md) | [Deutsch](README.de.md) | [Français](README.fr.md) | 日本語 | [한국어](README.ko.md) | [Русский](README.ru.md)

<sub>アプリの対応言語：中文 · English · Deutsch · Français · 日本語 · 한국어 · Русский</sub>

[![GitHub release](https://img.shields.io/github/v/release/SuXinxin520/AIChat?label=release)](https://github.com/SuXinxin520/AIChat/releases)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](./LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/SuXinxin520/AIChat?style=social)](https://github.com/SuXinxin520/AIChat/stargazers)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](#)

[ダウンロード](https://github.com/SuXinxin520/AIChat/releases) · [機能](#機能) · [データとプライバシー](#データとプライバシー) · [ビルド](#ビルド)

</div>

## 概要

AIChat は、AI キャラクターとチャットできる Android アプリです。キャラクターごとにアバター、名前、プロンプトを設定し、自分の OpenAI 互換 API に接続して会話を始められます。

OpenAI、Claude、DeepSeek、Volcengine（Doubao）、Gemini、Qwen、Kimi に対応し、任意のカスタムエンドポイントも利用できます。チャット、アバター、添付ファイルは選択したフォルダに保存され、独自サーバーを経由しません。メッセージは設定した API プロバイダーにのみ送信されます。

Kotlin と Jetpack Compose（Material 3）で構築。APK は約 1.4 MB です。

## 機能

- **複数キャラクター**：キャラクターごとにアバター、名前、プロンプトを設定し、システムプロンプトとして使用
- **ユーザープロフィール**：アバター、名前、プロンプトをキャラクター設定とともにモデルへ送信
- **プロバイダープリセット**：OpenAI / Claude / DeepSeek / Volcengine（Doubao）/ Gemini / Qwen / Kimi。エンドポイントとモデルを自動入力
- **キーへのショートカット**：API キー欄の左にある鍵アイコンからプロバイダーのキー管理ページを開けます
- **ストリーミング**：設定 → 一般でオフにできます
- **タイプライターアニメーション**：返信が 1 文字ずつ表示されます。水滴とカーソルのアニメーションは個別に切り替え可能
- **メッセージ管理**：各メッセージ下の行から削除できます。メッセージ横の空白を長押しすると複数選択になり、長押ししたままドラッグして素早く追加、下部バーから一括削除できます。テキストの長押しによる単語選択もそのまま使えます
- **入力**：プロンプト欄はフローティングラベル、内部スクロール、ドラッグ可能なスクロールバーを備えています
- **ローカル保存**：初回起動時にフォルダを選択。チャット、アバター、添付ファイル、設定がそこに保存されます。アンインストールしてもフォルダは残り、再インストール後に同じフォルダを選べば復元できます
- **言語とテーマ**：中国語 / 英語 / ドイツ語 / フランス語 / 日本語 / 韓国語 / ロシア語。ライト・ダークはシステムに追従
- **未保存の保護**：変更したフォームを離れるときに破棄するか保存するかを確認します

## ダウンロード

[Releases](https://github.com/SuXinxin520/AIChat/releases) から最新の `AIChat-x.y.z.apk` をダウンロードしてください。

Android 8.0（API 26）以上が必要です。

> 0.0.3 から正式な署名キーを使用しており、以前のビルドとは異なります。古いバージョンがインストールされている場合は、先にアンインストールしてください。データは選択した保存フォルダに残るため、再インストール後に同じフォルダを選択すれば復元できます。

## 使い方

1. 初回起動時にデータ保存用のローカルフォルダを選択（後で「設定 → ストレージ」から変更可能）
2. 「設定 → API」を開き、プロバイダーを選択するかカスタムエンドポイントを入力して API キーを貼り付け。URL 欄の左にある鍵アイコンからプロバイダーのキーページを開けます
3. ホームに戻り、キャラクターを追加または選択してチャットを開始

## データとプライバシー

- 設定、チャット、アバター、添付ファイルはすべて選択したフォルダに保存され、アプリ内には小さな設定キャッシュのみが残ります
- 独自サーバーへのアップロードはありません
- メッセージは設定した API プロバイダーにのみ送信されます
- アンインストールしても保存フォルダは削除されません。再インストール後に同じフォルダを選択してください

## ビルド

必要なもの：JDK 17、Android SDK 35。

```bash
./gradlew assembleRelease
```

APK は `app/build/outputs/apk/release/app-release.apk` に出力されます。Release ビルドは R8 を有効にし、約 1.4 MB です。

署名情報はプロジェクト直下の `keystore.properties` から読み込みます（git 管理外）。

```properties
storeFile=/path/to/your/release.jks
storePassword=******
keyAlias=aichat
keyPassword=******
```

このファイルがない場合は未署名の Release APK が出力されるため、独自の署名を設定できます。

## ライセンス

本プロジェクトは [PolyForm Noncommercial License 1.0.0](./LICENSE) に基づきます。

- **非商用利用**（個人利用、学習、研究など）：無料
- **商用利用には別途ライセンスが必要**です。社内利用、商用製品への組み込み、有料サービス提供などを含みます

商用ライセンスのお問い合わせ：`SuXinxin520@users.noreply.github.com`

## 作者

苏昕昕
