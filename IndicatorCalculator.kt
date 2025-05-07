class IndicatorCalculator @Inject constructor() {
    
    fun calculateAll(candles: List<CandleData>): Indicators {
        val closes = candles.map { it.close }
        val volumes = candles.map { it.volume }
        
        return Indicators(
            ema20 = calculateEMA(closes, 20).last(),
            ema50 = calculateEMA(closes, 50).last(),
            ema100 = calculateEMA(closes, 100).last(),
            ema200 = calculateEMA(closes, 200).last(),
            rsi = calculateRSI(closes, 14).last(),
            macd = calculateMACD(closes),
            sma50 = calculateSMA(closes, 50).last(),
            atr = calculateATR(candles, 14).last()
        )
    }
    
    fun calculateEMA(values: List<Double>, period: Int): List<Double> {
        if (values.size < period) return emptyList()
        
        val multiplier = 2.0 / (period + 1)
        val ema = mutableListOf<Double>()
        ema.add(values.take(period).average())
        
        for (i in period until values.size) {
            val currentEMA = (values[i] - ema.last()) * multiplier + ema.last()
            ema.add(currentEMA)
        }
        
        return ema
    }
    
    fun calculateSMA(values: List<Double>, period: Int): List<Double> {
        return values.windowed(period, 1, partialWindows = false) { window ->
            window.average()
        }
    }
    
    fun calculateRSI(values: List<Double>, period: Int): List<Double> {
        if (values.size <= period) return emptyList()
        
        val gains = mutableListOf<Double>()
        val losses = mutableListOf<Double>()
        
        for (i in 1 until values.size) {
            val change = values[i] - values[i - 1]
            gains.add(if (change > 0) change else 0.0)
            losses.add(if (change < 0) abs(change) else 0.0)
        }
        
        val avgGain = gains.take(period).average()
        val avgLoss = losses.take(period).average()
        
        val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
        val rsi = mutableListOf(100.0 - (100.0 / (1 + rs)))
        
        for (i in period until gains.size) {
            val currentAvgGain = (avgGain * (period - 1) + gains[i]) / period
            val currentAvgLoss = (avgLoss * (period - 1) + losses[i]) / period
            val currentRS = if (currentAvgLoss == 0.0) 100.0 else currentAvgGain / currentAvgLoss
            rsi.add(100.0 - (100.0 / (1 + currentRS)))
        }
        
        return rsi
    }
    
    fun calculateMACD(values: List<Double>): MACDData {
        val ema12 = calculateEMA(values, 12)
        val ema26 = calculateEMA(values, 26)
        
        if (ema12.size < 26 || ema26.size < 26) return MACDData.empty()
        
        val macdLine = ema12.zip(ema26).map { (e12, e26) -> e12 - e26 }
        val signalLine = calculateEMA(macdLine, 9)
        
        val histogram = if (signalLine.isNotEmpty()) {
            macdLine.zip(signalLine).map { (macd, signal) -> macd - signal }
        } else emptyList()
        
        return MACDData(
            macdLine = macdLine.last(),
            signalLine = signalLine.lastOrNull() ?: 0.0,
            histogram = histogram.lastOrNull() ?: 0.0
        )
    }
    
    fun calculateATR(candles: List<CandleData>, period: Int): List<Double> {
        if (candles.size < period + 1) return emptyList()
        
        val trueRanges = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val current = candles[i]
            val previous = candles[i - 1]
            val highLow = current.high - current.low
            val highClose = abs(current.high - previous.close)
            val lowClose = abs(current.low - previous.close)
            trueRanges.add(maxOf(highLow, highClose, lowClose))
        }
        
        val atr = mutableListOf(trueRanges.take(period).average())
        for (i in period until trueRanges.size) {
            atr.add((atr.last() * (period - 1) + trueRanges[i]) / period)
        }
        
        return atr
    }
    
    fun checkGoldenCross(emaShort: Double, emaLong: Double): Boolean {
        return emaShort > emaLong
    }
    
    fun calculatePullbackLevels(candles: List<CandleData>): PullbackLevels {
        val recentHigh = candles.maxOf { it.high }
        val recentLow = candles.minOf { it.low }
        val range = recentHigh - recentLow
        
        return PullbackLevels(
            level0 = recentHigh,
            level0236 = recentHigh - range * 0.236,
            level0382 = recentHigh - range * 0.382,
            level05 = recentHigh - range * 0.5,
            level0618 = recentHigh - range * 0.618,
            level0786 = recentHigh - range * 0.786,
            level1 = recentLow
        )
    }
    
    // ... other indicator calculation methods
}

data class Indicators(
    val ema20: Double,
    val ema50: Double,
    val ema100: Double,
    val ema200: Double,
    val rsi: Double,
    val macd: MACDData,
    val sma50: Double,
    val atr: Double
)

data class MACDData(
    val macdLine: Double,
    val signalLine: Double,
    val histogram: Double
) {
    companion object {
        fun empty() = MACDData(0.0, 0.0, 0.0)
    }
}

data class PullbackLevels(
    val level0: Double,
    val level0236: Double,
    val level0382: Double,
    val level05: Double,
    val level0618: Double,
    val level0786: Double,
    val level1: Double
)