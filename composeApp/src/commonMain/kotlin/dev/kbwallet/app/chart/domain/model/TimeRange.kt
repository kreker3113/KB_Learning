package dev.kbwallet.app.chart.domain.model

enum class TimeRange(
    val label: String,
    val binanceInterval: String,
    val limit: Int,
    val millisPerCandle: Long,
    // CoinGecko's /coins/{id}/ohlc endpoint doesn't take a candle-size
    // parameter — it auto-picks granularity from `days` alone: 1-2 days ->
    // 30m candles, 3-30 days -> 4h candles, 31+ days -> 4-day candles. Giving
    // ONE_HOUR/FOUR_HOURS/ONE_DAY the same `days` value (as a straight port
    // of the old Binance-interval scheme once did) made those three ranges
    // fetch the identical response — same data plotted three times. Each
    // value below is picked to land in a different granularity bucket (or at
    // least a different window within one) so the ranges actually differ.
    val coinGeckoDays: String,
) {
    ONE_HOUR(   "1H", "1h",  72,        3_600_000L, "1"),
    FOUR_HOURS( "4H", "4h",  84,       14_400_000L, "7"),
    ONE_DAY(    "1D", "1d",  90,       86_400_000L, "30"),
    ONE_WEEK(   "1W", "1w",  52,      604_800_000L, "90"),
    ONE_MONTH(  "1M", "1M",  60,    2_592_000_000L, "180"),
    ONE_YEAR(   "1Y", "1w",  52,   31_536_000_000L, "365"),
}
