# News & Finance Dashboard

Applicazione Android che combina notizie di attualita e monitoraggio criptovalute in un'unica dashboard.

Progetto d'esame per il corso *Laboratorio di Programmazione di Sistemi Mobili* (a.a. 2025/26) — Universita di Bologna.

## Requisiti

- Android Studio Ladybug (2024.2.1) o successivo
- JDK 17
- Android SDK con API level 35
- Emulatore o dispositivo con Android 8.0+ (API 26)

## Setup

1. Clonare il repository
2. Aprire il progetto in Android Studio
3. Sincronizzare Gradle ed eseguire l'app

## API utilizzate

| API | Scopo | Autenticazione |
|-----|-------|----------------|
| [NewsAPI](https://newsapi.org) | Notizie di attualita | Chiave API (gratuita) |
| [CoinGecko](https://www.coingecko.com/en/api) | Prezzi criptovalute | Nessuna (tier gratuito) |

## Stack tecnologico

Kotlin, Jetpack Compose, MVVM, Hilt, Retrofit, Room, WorkManager, Coroutines + Flow, Navigation Compose, Coil.

## Autori

Progetto realizzato da un gruppo di 2 studenti.
