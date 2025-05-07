data class CandleData(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val time: Long
)

data class VolumeData(
    val symbol: String,
    val values: List<Double>,
    val times: List<Long>
)

data class Coin(
    val symbol: String,
    val name: String,
    val price: Double,
    val change24h: Double,
    val marketCap: Double,
    val iconUrl: String
) {
    val priceFormatted: String
        get() = "%.4f".format(price)
    
    val change24hFormatted: String
        get() = "%.2f%%".format(change24h)
}

enum class TrendDirection {
    UPTREND, DOWNTREND, SIDEWAYS
}

data class EmaAlignment(
    val ema20Above50: Boolean,
    val ema50Above100: Boolean,
    val ema100Above200: Boolean,
    val allAligned: Boolean
)

data class EmaProximity(
    val toEma20: Double,  // Percentage distance
    val toEma50: Double,
    val toEma100: Double,
    val inZone: Boolean  // Within 2% of any EMA
)

data class FibRetracement(
    val to0382: Double,
    val to05: Double,
    val to0618: Double,
    val inZone: Boolean  // Within 1% of any Fib level
)

data class RsiConditions(
    val rsi15m: Double,
    val rsi1H: Double,
    val rsi15mAbove50: Boolean,
    val rsi1HBetween40and60: Boolean,
    val confirmed: Boolean
)

data class MacdConditions(
    val macdLine: Double,
    val signalLine: Double,
    val histogram: Double,
    val macdAboveSignal: Boolean,
    val histogramIncreasing: Boolean,
    val confirmed: Boolean
)