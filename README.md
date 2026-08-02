<div align="center">

# KB Wallet

**A Kotlin Multiplatform crypto portfolio & paper-trading app**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20iOS-lightgrey)]()
[![Ktor](https://img.shields.io/badge/backend-Ktor-orange?logo=ktor&logoColor=white)](https://ktor.io)

</div>

> **⚠️ Educational project.** KB Wallet is a learning/demo app. Nothing in it is financial advice, and the trading simulator uses fictional balances — no real money or real exchange is involved.

## What it is

KB Wallet is a Compose Multiplatform app (Android + iOS from one Kotlin codebase) for tracking a crypto portfolio, practicing trades in a simulated market, and learning the fundamentals of crypto — from "what is a blockchain" to reading candlestick charts.

## Features

- **Portfolio tracking** — holdings, live prices, P&L, transaction history
- **Buy / Sell flow** with a simulated balance — no real funds ever move
- **Trading simulator** — a background market engine that fills limit/stop orders against live-ish price ticks, so you can practice order types risk-free
- **Charts** — line and Japanese candlestick views with pan/zoom, SMA overlay, and multiple timeframes
- **Watchlist** — track coins you don't hold yet
- **Crypto Library** — a 17-topic reference built into the app, from beginner basics (wallets, private keys, exchanges, stablecoins) to advanced topics (DeFi, tokenomics, leverage, taxes)
- **Biometric login** and a lightweight auth server (Ktor) for account security
- **Analytics** — realized/unrealized P&L breakdown

## Tech stack

| Layer | Choice |
|---|---|
| UI | Compose Multiplatform (Material 3) |
| Language | Kotlin Multiplatform (Android + iOS) |
| DI | Koin |
| Networking | Ktor Client |
| Persistence | Room (KMP) |
| Images | Coil 3 |
| Backend | Ktor server (auth, hashing, tokens) |
| Serialization | kotlinx.serialization / kotlinx.datetime |

## Project structure

```
KB_Learning/
├── composeApp/            # Shared KMP client
│   └── src/
│       ├── commonMain/    # Shared UI + business logic (feature packages under app/)
│       ├── androidMain/   # Android-specific implementations
│       └── iosMain/       # iOS-specific implementations
├── iosApp/                # iOS app entry point (SwiftUI shell hosting Compose)
└── server/                # Ktor backend: auth, user accounts, token issuing
```

Client code under `composeApp/src/commonMain/kotlin/dev/kbwallet/app/` is organized by feature (`portfolio`, `trade`, `chart`, `coins`, `watchlist`, `history`, `library`, `analytics`, `simulator`, `profile`, `dashboard`), each following a rough data → domain → presentation layering, wired together with Koin.

## Getting started

**Prerequisites**
- JDK 21
- Android Studio (latest stable) for Android
- Xcode + a Mac for the iOS target (Kotlin/Native's iOS targets can't be built on Linux/Windows)

**Build & run**

```bash
# Android debug build
./gradlew :composeApp:assembleDebug

# Run backend server locally
./gradlew :server:run

# Run tests
./gradlew :composeApp:allTests
```

For iOS, open `iosApp/iosApp.xcodeproj` in Xcode and run — it hosts the shared Compose UI via the `ComposeApp` framework produced by the `:composeApp` module.

## Learn more

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
