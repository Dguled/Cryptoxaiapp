class IndicatorOptimizer @Inject constructor(
    private val indicatorCalculator: IndicatorCalculator
) {
    private val calculationCache = mutableMapOf<String, Indicators>()
    
    suspend fun calculateOptimized(
        candles: List<CandleData>,
        symbol: String,
        interval: String
    ): Indicators {
        val cacheKey = "$symbol-$interval-${candles.hashCode()}"
        
        // Check cache first
        calculationCache[cacheKey]?.let {
            return it
        }
        
        // Use coroutine dispatcher optimized for CPU-bound work
        return withContext(Dispatchers.Default) {
            // Calculate in parallel where possible
            val deferredEma20 = async { indicatorCalculator.calculateEMA(candles.map { it.close }, 20) }
            val deferredEma50 = async { indicatorCalculator.calculateEMA(candles.map { it.close }, 50) }
            val deferredRsi = async { indicatorCalculator.calculateRSI(candles.map { it.close }, 14) }
            val deferredMacd = async { indicatorCalculator.calculateMACD(candles.map { it.close }) }
            
            // Wait for all calculations to complete
            val ema20 = deferredEma20.await()
            val ema50 = deferredEma50.await()
            val rsi = deferredRsi.await()
            val macd = deferredMacd.await()
            
            // Create result
            val result = Indicators(
                ema20 = ema20.last(),
                ema50 = ema50.last(),
                ema100 = 0.0, // Not calculated for optimization
                ema200 = 0.0, // Not calculated for optimization
                rsi = rsi.last(),
                macd = macd,
                sma50 = 0.0, // Not calculated for optimization
                atr = 0.0 // Not calculated for optimization
            )
            
            // Cache result
            calculationCache[cacheKey] = result
            result
        }
    }
    
    fun clearCache() {
        calculationCache.clear()
    }
}