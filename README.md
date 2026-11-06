<div align="center">

# 爱聊 AIChat

AI Chat Reimagined // 和你的 AI 角色聊天

简体中文 | [English](README.en.md)

[![GitHub release](https://img.shields.io/github/v/release/SuXinxin520/AIChat?label=release)](https://github.com/SuXinxin520/AIChat/releases)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](./LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/SuXinxin520/AIChat?style=social)](https://github.com/SuXinxin520/AIChat/stargazers)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](#)

[下载安装](https://github.com/SuXinxin520/AIChat/releases) · [功能特性](#功能特性) · [数据与隐私](#数据与隐私) · [构建说明](#构建)

</div>

## 项目简介

爱聊 AIChat 是一款 Android 端的 AI 角色聊天应用：为每个角色设定头像、名称和提示词，接入你自己的 OpenAI 兼容 API，随时开始对话。

支持 OpenAI、Claude、DeepSeek、火山引擎（豆包）、Gemini、Qwen、Kimi 等服务商，也支持任意自定义地址。聊天记录、头像和附件都保存在你自己选择的文件夹里，不经过任何自建服务器，聊天内容只会发送到你配置的 API。

界面使用 Kotlin + Jetpack Compose（Material 3）实现，APK 约 1.4 MB。

## 功能特性

- **多角色**：每个角色可设置头像、名称和提示词，聊天时自动作为系统提示带入
- **用户资料**：头像、名称、提示词同样会传给模型，和角色设定一起组成上下文
- **服务商预设**：OpenAI / Claude / DeepSeek / 火山引擎（豆包）/ Gemini / Qwen / Kimi，地址和模型一键填入，也可自行填写
- **密钥直达**：API Key 输入框左侧的钥匙图标可直接跳转到对应服务商开放平台的密钥管理页
- **流式输出**：可在「设置 → 通用」关闭，改为整段返回后再逐字显示
- **逐字动画**：回复逐字弹出并回弹，页面自动跟随；可单独开关水滴落和光标渐隐动画
- **消息管理**：每条消息下方可删除；长按消息外侧空白进入多选，长按拖动可快速加选，底部一键删除；长按文字仍可选词复制
- **输入体验**：提示词输入框使用浮动标签，支持框内滚动和可拖动滚动条
- **本地存储**：首次启动选择一个文件夹，聊天记录、头像、附件和设置都镜像在里面；卸载不删除，重装后重新选择同一文件夹即可恢复
- **多语言与主题**：中 / 英 / 德 / 法 / 日 / 韩 / 俄，浅色 / 深色跟随系统
- **表单保护**：修改未保存时离开会弹窗确认，可选择直接退出或保存并退出

## 下载

前往 [Releases](https://github.com/SuXinxin520/AIChat/releases) 下载最新的 `AIChat-x.y.z.apk`。

要求 Android 8.0（API 26）及以上。

> 0.0.3 起使用正式签名，与更早版本的签名不同。如果你之前安装过旧版本，需要先卸载再安装；卸载前请确认数据已保存在你选择的存储文件夹中，重装后重新选择同一个文件夹即可恢复。

## 使用

1. 首次启动时选择一个本地文件夹作为数据目录（之后可以在「设置 → 存储」中更换）
2. 打开「设置 → API」，选择服务商或填写自定义地址，粘贴 API Key；点击地址框左侧的钥匙图标可以快速打开对应平台的密钥页面
3. 回到首页，添加角色或选择已有角色，开始聊天

## 数据与隐私

- 所有配置、聊天记录、头像和附件都保存在你选择的本地文件夹中，应用内部只保留少量配置缓存
- 数据不会上传到任何自建服务器
- 聊天内容只会发送到你配置的 API 服务商
- 卸载应用不会删除存储文件夹；重装后重新选择同一文件夹即可恢复

## 构建说明

环境要求：JDK 17、Android SDK 35。

```bash
./gradlew assembleRelease
```

产物位于 `app/build/outputs/apk/release/app-release.apk`，Release 开启 R8，体积约 1.4 MB。

签名信息读取项目根目录的 `keystore.properties`（已加入 `.gitignore`，不会提交）：

```properties
storeFile=/path/to/your/release.jks
storePassword=******
keyAlias=aichat
keyPassword=******
```

没有该文件时会产出未签名的 Release APK，方便自行配置签名。

## 许可

本项目采用 [PolyForm Noncommercial License 1.0.0](./LICENSE)：

- **非商业用途**（个人使用、学习、研究等）：免费
- **任何商业用途都需要单独获得授权**，包括但不限于公司内部使用、集成进商业产品、提供付费服务

商业授权请联系：`SuXinxin520@users.noreply.github.com`

## 作者

苏昕昕
