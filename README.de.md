<div align="center">

# AIChat

AI Chat Reimagined // Chatte mit deinen KI-Charakteren

[简体中文](README.md) | [English](README.en.md) | Deutsch | [Français](README.fr.md) | [日本語](README.ja.md) | [한국어](README.ko.md) | [Русский](README.ru.md)

<sub>App-Sprachen: 中文 · English · Deutsch · Français · 日本語 · 한국어 · Русский</sub>

[![GitHub release](https://img.shields.io/github/v/release/SuXinxin520/AIChat?label=release)](https://github.com/SuXinxin520/AIChat/releases)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](./LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/SuXinxin520/AIChat?style=social)](https://github.com/SuXinxin520/AIChat/stargazers)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](#)

[Download](https://github.com/SuXinxin520/AIChat/releases) · [Funktionen](#funktionen) · [Daten und Datenschutz](#daten-und-datenschutz) · [Build](#build)

</div>

## Überblick

AIChat ist eine Android-App für Chats mit KI-Charakteren. Erstelle einen Charakter mit eigenem Avatar, Namen und Prompt, verbinde deine eigene OpenAI-kompatible API und leg los.

Die App unterstützt OpenAI, Claude, DeepSeek, Volcengine (Doubao), Gemini, Qwen und Kimi sowie beliebige eigene Endpunkte. Chats, Avatare und Anhänge liegen in einem Ordner deiner Wahl – nichts läuft über einen eigenen Server, und Nachrichten werden nur an den konfigurierten API-Anbieter gesendet.

Entwickelt mit Kotlin und Jetpack Compose (Material 3). Die APK ist etwa 1,4 MB groß.

## Funktionen

- **Mehrere Charaktere**: Avatar, Name und Prompt pro Charakter, als System-Prompt übernommen
- **Benutzerprofil**: Avatar, Name und Prompt werden zusammen mit den Charaktereinstellungen an das Modell gesendet
- **Anbieter-Presets**: OpenAI / Claude / DeepSeek / Volcengine (Doubao) / Gemini / Qwen / Kimi, Endpunkt und Modelle werden automatisch eingetragen
- **Schlüssel-Shortcut**: Das Schlüsselsymbol links im API-Key-Feld öffnet die Schlüsselverwaltung des Anbieters
- **Streaming**: in Einstellungen → Allgemein abschaltbar
- **Schreibmaschinen-Animation**: Antworten erscheinen Zeichen für Zeichen; Tropfen- und Cursor-Animationen lassen sich getrennt schalten
- **Nachrichtenverwaltung**: Jede Nachricht lässt sich über die Zeile darunter löschen; langes Drücken auf den leeren Bereich neben Nachrichten startet die Mehrfachauswahl, langes Drücken und Ziehen fügt Nachrichten schnell hinzu, Löschen über die untere Leiste; langes Drücken auf Text markiert weiterhin Wörter
- **Eingabe**: Das Prompt-Feld nutzt ein schwebendes Label, scrollt intern und hat eine ziehbare Bildlaufleiste
- **Lokaler Speicher**: Beim ersten Start einen Ordner wählen; Chats, Avatare, Anhänge und Einstellungen werden dort gespiegelt. Deinstallieren löscht ihn nicht; nach Neuinstallation genügt derselbe Ordner
- **Sprachen und Theme**: Chinesisch / Englisch / Deutsch / Französisch / Japanisch / Koreanisch / Russisch, hell/dunkel nach System
- **Schutz vor Datenverlust**: Beim Verlassen eines geänderten Formulars fragt die App, ob verwerfen oder speichern

## Download

Lade die neueste `AIChat-x.y.z.apk` aus den [Releases](https://github.com/SuXinxin520/AIChat/releases).

Erfordert Android 8.0 (API 26) oder neuer.

> Ab 0.0.3 verwendet die App einen offiziellen Signaturschlüssel, der sich von älteren Builds unterscheidet. Wenn eine ältere Version installiert ist, deinstalliere sie zuerst. Deine Daten bleiben im gewählten Speicherordner; wähle nach der Neuinstallation denselben Ordner.

## Verwendung

1. Beim ersten Start einen lokalen Ordner als Datenverzeichnis wählen (später unter Einstellungen → Speicher änderbar)
2. Einstellungen → API öffnen, Anbieter wählen oder eigenen Endpunkt eingeben und API-Key einfügen; das Schlüsselsymbol links im URL-Feld öffnet die Schlüsselseite des Anbieters
3. Zurück zum Startbildschirm, Charakter hinzufügen oder öffnen und chatten

## Daten und Datenschutz

- Alle Einstellungen, Chats, Avatare und Anhänge liegen im gewählten Ordner; in der App bleibt nur ein kleiner Konfigurations-Cache
- Nichts wird auf einen eigenen Server hochgeladen
- Nachrichten werden nur an den konfigurierten API-Anbieter gesendet
- Deinstallieren löscht den Speicherordner nicht; nach Neuinstallation denselben Ordner wählen

## Build

Voraussetzungen: JDK 17 und Android SDK 35.

```bash
./gradlew assembleRelease
```

Die APK liegt unter `app/build/outputs/apk/release/app-release.apk`. Release-Builds aktivieren R8 und sind etwa 1,4 MB groß.

Signierinformationen werden aus `keystore.properties` im Projektstamm gelesen (bereits in `.gitignore`):

```properties
storeFile=/path/to/your/release.jks
storePassword=******
keyAlias=aichat
keyPassword=******
```

Ohne diese Datei entsteht eine unsignierte Release-APK, damit du deine eigene Signatur konfigurieren kannst.

## Lizenz

Dieses Projekt steht unter der [PolyForm Noncommercial License 1.0.0](./LICENSE):

- **Nichtkommerzielle Nutzung** (privat, Lernen, Forschung usw.): kostenlos
- **Jede kommerzielle Nutzung erfordert eine separate Lizenz**, einschließlich interner Nutzung im Unternehmen, Integration in kommerzielle Produkte oder kostenpflichtige Dienste

Für kommerzielle Lizenzen: `SuXinxin520@users.noreply.github.com`.

## Autor

Su Xinxin (苏昕昕)
