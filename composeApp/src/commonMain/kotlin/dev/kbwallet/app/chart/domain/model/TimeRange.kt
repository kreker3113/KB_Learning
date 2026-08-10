package dev.kbwallet.app.chart.domain.model

enum class TimeRange(
    val label: String,
    val binanceInterval: String,
    val limit: Int,
    val millisPerCandle: Long,
    val coinGeckoDays: String,
) {
    ONE_HOUR(   "1H", "1h",  72,        3_600_000L, "1"),
    FOUR_HOURS( "4H", "4h",  84,       14_400_000L, "1"),
    ONE_DAY(    "1D", "1d",  90,       86_400_000L, "1"),
    ONE_WEEK(   "1W", "1w",  52,      604_800_000L, "7"),
    ONE_MONTH(  "1M", "1M",  60,    2_592_000_000L, "30"),
    ONE_YEAR(   "1Y", "1w",  52,   31_536_000_000L, "365"),
}
