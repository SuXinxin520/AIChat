<div align="center">

# AIChat

AI Chat Reimagined // Chat with your AI characters

[简体中文](README.md) | English

[![GitHub release](https://img.shields.io/github/v/release/SuXinxin520/AIChat?label=release)](https://github.com/SuXinxin520/AIChat/releases)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](./LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/SuXinxin520/AIChat?style=social)](https://github.com/SuXinxin520/AIChat/stargazers)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](#)

[Download](https://github.com/SuXinxin520/AIChat/releases) · [Features](#features) · [Data & privacy](#data--privacy) · [Build](#build)

</div>

## Overview

AIChat is an Android AI character chat app. Create a character with its own avatar, name and prompt, connect your own OpenAI-compatible API, and start chatting.

It works with OpenAI, Claude, DeepSeek, Volcengine (Doubao), Gemini, Qwen and Kimi, and with any custom endpoint. Chats, avatars and attachments live in a folder you choose — nothing goes through a self-hosted server, and your messages are only sent to the API provider you configure.

Built with Kotlin and Jetpack Compose (Material 3). The APK is about 1.4 MB.

## Features

- **Multiple characters**: avatar, name and prompt per character, injected as the system prompt
- **User profile**: avatar, name and prompt are sent to the model together with the character settings
- **Provider presets**: OpenAI / Claude / DeepSeek / Volcengine (Doubao) / Gemini / Qwen / Kimi, with endpoints and models filled in automatically
- **Key shortcut**: the key icon on the left of the API key field opens the provider's key management page
- **Streaming**: can be disabled in Settings → General
- **Typewriter animation**: replies pop in character by character; the droplet and cursor-fade animations can be toggled separately
- **Message management**: delete any message from the row below it; long-press the blank area beside messages to enter multi-select, long-press and drag to add messages quickly, and delete them from the bottom bar; long-press text to select words as usual
- **Input experience**: the prompt field uses a floating label, scrolls internally, and has a draggable scrollbar
- **Local storage**: on first launch pick a folder; chats, avatars, attachments and settings are mirrored there. Uninstalling does not delete it, and picking the same folder after reinstalling restores everything
- **Languages and theme**: Chinese / English / German / French / Japanese / Korean / Russian, light and dark following the system
- **Unsaved changes guard**: leaving a modified form asks whether to discard or save

## Download

Get the latest `AIChat-x.y.z.apk` from [Releases](https://github.com/SuXinxin520/AIChat/releases).

Requires Android 8.0 (API 26) or newer.

> Starting with 0.0.3 the app uses a formal signing key, which differs from older builds. If you have an older version installed, uninstall it first. Your data stays in the storage folder you selected; pick the same folder after reinstalling to restore it.

## Usage

1. On first launch, choose a local folder as the data directory (it can be changed later in Settings → Storage)
2. Open Settings → API, pick a provider or enter a custom endpoint, and paste your API key; the key icon to the left of the URL field opens the provider's key page
3. Go back to the home screen, add a character or open an existing one, and start chatting

## Data & privacy

- All settings, chats, avatars and attachments are stored in the folder you choose; only a small configuration cache stays inside the app
- Nothing is uploaded to a self-hosted server
- Messages are only sent to the API provider you configure
- Uninstalling the app does not delete the storage folder; pick the same folder after reinstalling to restore your data

## Build

Requirements: JDK 17 and Android SDK 35.

```bash
./gradlew assembleRelease
```

The APK is written to `app/build/outputs/apk/release/app-release.apk`. Release builds enable R8 and are about 1.4 MB.

Signing information is read from `keystore.properties` in the project root (already ignored by git):

```properties
storeFile=/path/to/your/release.jks
storePassword=******
keyAlias=aichat
keyPassword=******
```

Without that file the build produces an unsigned release APK, so you can configure your own signing.

## License

This project is licensed under the [PolyForm Noncommercial License 1.0.0](./LICENSE):

- **Noncommercial use** (personal use, learning, research, etc.): free
- **Any commercial use requires a separate license**, including internal company use, integration into commercial products, or paid services

For commercial licensing, contact `SuXinxin520@users.noreply.github.com`.

## Author

Su Xinxin (苏昕昕)
