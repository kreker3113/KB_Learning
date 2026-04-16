package dev.kbwallet.app.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object Portfolio

@Serializable
object Coins

@Serializable
data class Buy(val coinId: String)

@Serializable
data class Sell(val coinId: String)