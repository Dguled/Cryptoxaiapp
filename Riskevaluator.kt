class RiskEvaluator @Inject constructor(
    private val indicatorCalculator: IndicatorCalculator
) {
    
    fun evaluateRisk(
        candles15m: List<CandleData>,
        candles1H: List<CandleData>,
        candles4H: List<CandleData>,
        indicators15m: Indicators,
        indicators1H: Indicators,
        indicators4H: Indicators
    ): RiskAssessment {
        val currentPrice = candles15m.last().close
        
        // Calculate stop loss levels
        val stopLossLevels = calculateStopLossLevels(
            candles15m = candles15m,
            candles1H = candles1H,
            ema50_15m = indicators15m.ema50,
            ema50_1H = indicators1H.ema50,
            atr = indicators15m.atr
        )
        
        // Calculate take profit levels
        val takeProfitLevels = calculateTakeProfitLevels(
            currentPrice = currentPrice,
            candles1H = candles1H,
            atr = indicators15m.atr
        )
        
        // Calculate risk-reward ratio
        val riskRewardRatio = if (stopLossLevels.primary > 0) {
            (takeProfitLevels.primary - currentPrice) / (currentPrice - stopLossLevels.primary)
        } else 0.0
        
        return RiskAssessment(
            stopLossLevels = stopLossLevels,
            takeProfitLevels = takeProfitLevels,
            riskRewardRatio = riskRewardRatio,
            atr = indicators15m.atr,
            volatility = calculateVolatility(candles15m)
        )
    }
    
    private fun calculateStopLossLevels(
        candles15m: List<CandleData>,
        candles1H: List<CandleData>,
        ema50_15m: Double,
        ema50_1H: Double,
        atr: Double
    ): StopLossLevels {
        val currentPrice = candles15m.last().close
        val recentLow15m = candles15m.takeLast(10).minOf { it.low }
        val recentLow1H = candles1H.takeLast(5).minOf { it.low }
        
        // Primary SL: Below recent swing low or EMA50
        val primarySL = minOf(
            recentLow15m * 0.995,  // 0.5% below recent low
            ema50_15m * 0.99,      // 1% below EMA50
            currentPrice - atr * 1.5  // 1.5 ATR below current price
        )
        
        // Secondary SL: Below key 1H levels
        val secondarySL = minOf(
            recentLow1H * 0.99,
            ema50_1H * 0.985,
            currentPrice - atr * 2
        )
        
        return StopLossLevels(
            primary = primarySL,
            secondary = secondarySL,
            aggressive = currentPrice - atr * 1,
            conservative = currentPrice - atr * 2
        )
    }
    
    private fun calculateTakeProfitLevels(
        currentPrice: Double,
        candles1H: List<CandleData>,
        atr: Double
    ): TakeProfitLevels {
        val recentHigh1H = candles1H.takeLast(20).maxOf { it.high }
        
        // Primary TP: 1:2 risk-reward or next resistance
        val primaryTP = maxOf(
            currentPrice + atr * 3,  // 3 ATR above
            currentPrice * 1.02,     // 2% above
            recentHigh1H * 0.995     // Just below recent high
        )
        
        // Secondary TP: More aggressive targets
        val secondaryTP = maxOf(
            currentPrice + atr * 5,
            currentPrice * 1.05,
            recentHigh1H * 1.02
        )
        
        return TakeProfitLevels(
            primary = primaryTP,
            secondary = secondaryTP,
            aggressive = currentPrice + atr * 4,
            conservative = currentPrice + atr * 2
        )
    }
    
    private fun calculateVolatility(candles: List<CandleData>): Double {
        if (candles.size < 20) return 0.0
        
        val changes = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            changes.add(abs(candles[i].close - candles[i - 1].close) / candles[i - 1].close)
        }
        
        return changes.takeLast(20).average()
    }
}

data class RiskAssessment(
    val stopLossLevels: StopLossLevels,
    val takeProfitLevels: TakeProfitLevels,
    val riskRewardRatio: Double,
    val atr: Double,
    val volatility: Double
)

data class StopLossLevels(
    val primary: Double,
    val secondary: Double,
    val aggressive: Double,
    val conservative: Double
)

data class TakeProfitLevels(
    val primary: Double,
    val secondary: Double,
    val aggressive: Double,
    val conservative: Double
)