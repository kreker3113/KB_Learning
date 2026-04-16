package dev.kbwallet.app.coins.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoinsResponseDto(
    var data: CoinsListDto
)
@Serializable
data class CoinsListDto(
    val coins: List<CoinItemDto>
)
@Serializable
data class CoinItemDto(
    val uuid: String,
    val symbol: String,
    val name: String,
    val iconUrl: String,
    val price: Double,
    val rank: Int,
    val change: Double,
)