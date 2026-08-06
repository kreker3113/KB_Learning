package dev.kbwallet.app.core.navigation

import kotlinx.serialization.Serializable

// ── App entry ──
@Serializable
object Biometric

// ── Main tabs (BottomNavigationBar) ──
@Serializable
object Dashboard

@Serializable
object Portfolio

@Serializable
object History

@Serializable
object Profile

// ── Secondary screens ──
@Serializable
object Coins

@Serializable
data class Buy(val coinId: String)

@Serializable
data class Sell(val coinId: String)

@Serializable
data class CryptoChart(val coinId: String, val coinName: String)

// ── Profile sub-screens ──
@Serializable
object EditProfile

@Serializable
object NotificationSettings

@Serializable
object SecuritySettings

@Serializable
object HelpSupport

@Serializable
object LanguageSettings

// ── Trading Simulator additions ──
@Serializable
object PnLAnalytics

@Serializable
object ActiveOrders

@Serializable
object Simulator

// ── Knowledge library ──
@Serializable
object Library

@Serializable
data class Topic(val topicId: String)
