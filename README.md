# 爱聊 AIChat

一款 Android 端的 AI 聊天应用。自带 API 配置，支持多家主流模型服务商，所有数据只存在本机。

## 功能

- **多角色**：为每个角色设定头像、名称和提示词，聊天时自动作为系统提示带入
- **用户资料**：头像、名称、提示词，同样会传给模型
- **服务商预设**：OpenAI / Claude / DeepSeek / Gemini / Qwen / Kimi，也可自行填写地址
- **流式输出**：可在「设置 → 通用」里关闭，改为整段返回后再逐字显示
- **逐字动画**：回复逐字弹出并回弹到正常大小，页面自动跟随
- **聊天记录**：本地保存，支持清空记录（保留角色）；未发出的内容会存成草稿
- **界面**：中英双语、浅色/深色主题，默认跟随系统

## 构建

环境要求：JDK 17、Android SDK 35。

```bash
./gradlew assembleRelease
```

产物位于 `app/build/outputs/apk/release/app-release.apk`。

> 当前 release 构建沿用 debug 签名，只为了方便自行安装测试。正式分发前请在 `app/build.gradle.kts` 中换成自己的签名配置。

## 数据与隐私

- 所有配置、聊天记录、头像和附件都保存在本机（SharedPreferences 与应用私有目录）
- 不会上传到任何第三方服务器
- 聊天内容只会发送到你自己配置的 API 服务商

## 许可

本项目采用 [PolyForm Noncommercial License 1.0.0](./LICENSE)：

- **非商业用途**（个人使用、学习、研究等）：免费
- **任何商业用途都需要单独获得授权**，包括但不限于公司内部使用、集成进商业产品、提供付费服务

商业授权请联系：`SuXinxin520@users.noreply.github.com`

## 作者

苏昕昕
