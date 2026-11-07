<div align="center">

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="assets/title-dark.png">
    <img src="assets/title-light.png" alt="AIChat" height="72">
  </picture>
</p>

AI Chat Reimagined // Discutez avec vos personnages IA

[简体中文](README.md) | [English](README.en.md) | [Deutsch](README.de.md) | Français | [日本語](README.ja.md) | [한국어](README.ko.md) | [Русский](README.ru.md)

<sub>Langues de l'application : 中文 · English · Deutsch · Français · 日本語 · 한국어 · Русский</sub>

[![GitHub release](https://img.shields.io/github/v/release/SuXinxin520/AIChat?label=release)](https://github.com/SuXinxin520/AIChat/releases)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](./LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/SuXinxin520/AIChat?style=social)](https://github.com/SuXinxin520/AIChat/stargazers)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](#)

[Téléchargement](https://github.com/SuXinxin520/AIChat/releases) · [Fonctionnalités](#fonctionnalités) · [Données et confidentialité](#données-et-confidentialité) · [Compilation](#compilation)

</div>

## Aperçu

AIChat est une application Android de discussion avec des personnages IA. Créez un personnage avec son avatar, son nom et son prompt, connectez votre propre API compatible OpenAI et commencez à discuter.

Elle prend en charge OpenAI, Claude, DeepSeek, Volcengine (Doubao), Gemini, Qwen et Kimi, ainsi que tout point de terminaison personnalisé. Les discussions, avatars et pièces jointes restent dans le dossier de votre choix — rien ne passe par un serveur auto-hébergé, et vos messages ne sont envoyés qu'au fournisseur d'API configuré.

Développée avec Kotlin et Jetpack Compose (Material 3). L'APK fait environ 1,4 Mo.

## Fonctionnalités

- **Plusieurs personnages** : avatar, nom et prompt par personnage, injectés comme prompt système
- **Profil utilisateur** : avatar, nom et prompt envoyés au modèle avec les réglages du personnage
- **Préréglages de fournisseurs** : OpenAI / Claude / DeepSeek / Volcengine (Doubao) / Gemini / Qwen / Kimi, avec endpoint et modèles préremplis
- **Raccourci clé** : l'icône de clé à gauche du champ de clé API ouvre la gestion des clés du fournisseur
- **Streaming** : désactivable dans Paramètres → Général
- **Animation machine à écrire** : les réponses apparaissent caractère par caractère ; les animations de goutte et de curseur se règlent séparément
- **Gestion des messages** : supprimez un message depuis la ligne située en dessous ; appuyez longuement sur la zone vide à côté des messages pour la sélection multiple, puis faites glisser pour en ajouter rapidement et supprimez depuis la barre du bas ; l'appui long sur le texte sélectionne toujours des mots
- **Saisie** : le champ de prompt utilise une étiquette flottante, défile en interne et dispose d'une barre de défilement déplaçable
- **Stockage local** : au premier lancement, choisissez un dossier ; discussions, avatars, pièces jointes et réglages y sont mis en miroir. La désinstallation ne le supprime pas ; après réinstallation, sélectionnez le même dossier
- **Langues et thème** : chinois / anglais / allemand / français / japonais / coréen / russe, clair et sombre selon le système
- **Protection des modifications** : en quittant un formulaire modifié, choisissez d'abandonner ou d'enregistrer

## Téléchargement

Téléchargez la dernière `AIChat-x.y.z.apk` dans les [Releases](https://github.com/SuXinxin520/AIChat/releases).

Nécessite Android 8.0 (API 26) ou version ultérieure.

> Depuis la 0.0.3, l'application utilise une clé de signature officielle, différente des anciennes versions. Si une version plus ancienne est installée, désinstallez-la d'abord. Vos données restent dans le dossier de stockage choisi ; sélectionnez le même dossier après réinstallation.

## Utilisation

1. Au premier lancement, choisissez un dossier local comme répertoire de données (modifiable dans Paramètres → Stockage)
2. Ouvrez Paramètres → API, choisissez un fournisseur ou saisissez un endpoint personnalisé, puis collez votre clé API ; l'icône de clé à gauche du champ d'URL ouvre la page des clés du fournisseur
3. Revenez à l'accueil, ajoutez ou ouvrez un personnage et discutez

## Données et confidentialité

- Tous les réglages, discussions, avatars et pièces jointes restent dans le dossier choisi ; seule une petite configuration reste dans l'application
- Rien n'est envoyé à un serveur auto-hébergé
- Les messages ne sont envoyés qu'au fournisseur d'API configuré
- La désinstallation ne supprime pas le dossier de stockage ; après réinstallation, sélectionnez le même dossier

## Compilation

Prérequis : JDK 17 et Android SDK 35.

```bash
./gradlew assembleRelease
```

L'APK se trouve dans `app/build/outputs/apk/release/app-release.apk`. Les builds Release activent R8 et font environ 1,4 Mo.

Les informations de signature sont lues depuis `keystore.properties` à la racine du projet (déjà ignoré par git) :

```properties
storeFile=/path/to/your/release.jks
storePassword=******
keyAlias=aichat
keyPassword=******
```

Sans ce fichier, un APK Release non signé est produit, ce qui vous permet de configurer votre propre signature.

## Licence

Ce projet est sous [PolyForm Noncommercial License 1.0.0](./LICENSE) :

- **Usage non commercial** (usage personnel, apprentissage, recherche, etc.) : gratuit
- **Tout usage commercial nécessite une licence séparée**, y compris l'usage interne en entreprise, l'intégration dans des produits commerciaux ou des services payants

Contact pour une licence commerciale : `SuXinxin520@users.noreply.github.com`.

## Auteur

Su Xinxin (苏昕昕)
