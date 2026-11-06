<div align="center">

# AIChat

AI Chat Reimagined // AI 캐릭터와 대화하세요

[简体中文](README.md) | [English](README.en.md) | [Deutsch](README.de.md) | [Français](README.fr.md) | [日本語](README.ja.md) | 한국어 | [Русский](README.ru.md)

<sub>앱 지원 언어: 中文 · English · Deutsch · Français · 日本語 · 한국어 · Русский</sub>

[![GitHub release](https://img.shields.io/github/v/release/SuXinxin520/AIChat?label=release)](https://github.com/SuXinxin520/AIChat/releases)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](./LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/SuXinxin520/AIChat?style=social)](https://github.com/SuXinxin520/AIChat/stargazers)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](#)

[다운로드](https://github.com/SuXinxin520/AIChat/releases) · [기능](#기능) · [데이터 및 개인정보](#데이터-및-개인정보) · [빌드](#빌드)

</div>

## 개요

AIChat은 AI 캐릭터와 대화할 수 있는 Android 앱입니다. 캐릭터마다 아바타, 이름, 프롬프트를 설정하고 자신의 OpenAI 호환 API에 연결해 대화를 시작하세요.

OpenAI, Claude, DeepSeek, Volcengine(Doubao), Gemini, Qwen, Kimi를 지원하며 사용자 지정 엔드포인트도 사용할 수 있습니다. 채팅, 아바타, 첨부 파일은 선택한 폴더에 저장되고 자체 서버를 거치지 않으며, 메시지는 설정한 API 공급자에게만 전송됩니다.

Kotlin과 Jetpack Compose(Material 3)로 제작되었습니다. APK는 약 1.4MB입니다.

## 기능

- **다중 캐릭터**: 캐릭터별 아바타, 이름, 프롬프트를 설정하고 시스템 프롬프트로 사용
- **사용자 프로필**: 아바타, 이름, 프롬프트를 캐릭터 설정과 함께 모델에 전달
- **공급자 프리셋**: OpenAI / Claude / DeepSeek / Volcengine(Doubao) / Gemini / Qwen / Kimi, 엔드포인트와 모델 자동 입력
- **키 바로가기**: API 키 입력란 왼쪽의 키 아이콘으로 공급자의 키 관리 페이지 열기
- **스트리밍**: 설정 → 일반에서 끌 수 있음
- **타자기 애니메이션**: 답변이 한 글자씩 나타남; 물방울과 커서 애니메이션은 각각 켜고 끌 수 있음
- **메시지 관리**: 각 메시지 아래 행에서 삭제; 메시지 옆 빈 공간을 길게 누르면 다중 선택, 길게 누른 채 드래그해 빠르게 추가, 하단 바에서 일괄 삭제; 텍스트 길게 누르기로 단어 선택도 유지
- **입력**: 프롬프트 입력란은 플로팅 라벨, 내부 스크롤, 드래그 가능한 스크롤바 제공
- **로컬 저장**: 첫 실행 시 폴더 선택; 채팅, 아바타, 첨부 파일, 설정이 그 폴더에 저장됨. 앱을 삭제해도 폴더는 남으며 재설치 후 같은 폴더를 선택하면 복원됨
- **언어와 테마**: 중국어 / 영어 / 독일어 / 프랑스어 / 일본어 / 한국어 / 러시아어, 밝게/어둡게 시스템 따름
- **변경 사항 보호**: 수정한 양식을 떠날 때 버리거나 저장할지 확인

## 다운로드

[Releases](https://github.com/SuXinxin520/AIChat/releases)에서 최신 `AIChat-x.y.z.apk`를 다운로드하세요.

Android 8.0(API 26) 이상이 필요합니다.

> 0.0.3부터 공식 서명 키를 사용하며 이전 빌드와 다릅니다. 이전 버전이 설치되어 있다면 먼저 삭제하세요. 데이터는 선택한 저장 폴더에 남아 있으며 재설치 후 같은 폴더를 선택하면 복원됩니다.

## 사용 방법

1. 첫 실행 시 데이터 디렉터리로 사용할 로컬 폴더 선택(나중에 설정 → 저장소에서 변경 가능)
2. 설정 → API를 열고 공급자를 선택하거나 사용자 지정 엔드포인트를 입력한 뒤 API 키 붙여넣기; URL 입력란 왼쪽의 키 아이콘으로 공급자의 키 페이지 열기
3. 홈으로 돌아가 캐릭터를 추가하거나 열고 대화 시작

## 데이터 및 개인정보

- 모든 설정, 채팅, 아바타, 첨부 파일은 선택한 폴더에 저장되며 앱에는 작은 설정 캐시만 남습니다
- 자체 서버로 업로드되지 않습니다
- 메시지는 설정한 API 공급자에게만 전송됩니다
- 앱을 삭제해도 저장 폴더는 삭제되지 않습니다; 재설치 후 같은 폴더를 선택하세요

## 빌드

요구 사항: JDK 17, Android SDK 35.

```bash
./gradlew assembleRelease
```

APK는 `app/build/outputs/apk/release/app-release.apk`에 생성됩니다. Release 빌드는 R8을 사용하며 약 1.4MB입니다.

서명 정보는 프로젝트 루트의 `keystore.properties`에서 읽습니다(git 제외).

```properties
storeFile=/path/to/your/release.jks
storePassword=******
keyAlias=aichat
keyPassword=******
```

이 파일이 없으면 서명되지 않은 Release APK가 생성되어 직접 서명을 설정할 수 있습니다.

## 라이선스

이 프로젝트는 [PolyForm Noncommercial License 1.0.0](./LICENSE)을 따릅니다.

- **비상업적 사용**(개인 사용, 학습, 연구 등): 무료
- **모든 상업적 사용에는 별도 라이선스가 필요합니다**, 회사 내부 사용, 상업 제품 통합, 유료 서비스 제공 포함

상업 라이선스 문의: `SuXinxin520@users.noreply.github.com`

## 작성자

쑤신신 (苏昕昕)
