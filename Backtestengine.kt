class BacktestEngine @Inject constructor(
    private val strategyEngine: StrategyEngine,
    private val binanceRepository: BinanceRepository,
    private val backtestDao: BacktestDao
) {
    suspend fun runBacktest(
        symbol: String,
        startTime: Long,
        endTime: Long,
        timeframe: String,
        initialBalance: Double
    ): BacktestResult {
        // Fetch historical data
        val candles = binanceRepository.getHistoricalData(symbol, timeframe, startTime, endTime)
        
        // Split data into training and testing sets (80/20)
        val splitIndex = (candles.size * 0.8).toInt()
        val trainingData = candles.subList(0, splitIndex)
        val testingData = candles.subList(splitIndex, candles.size)
        
        // Optimize parameters on training data
        val optimizedParams = optimizeParameters(trainingData)
        
        // Run backtest on testing data with optimized parameters
        val result = executeBacktest(testingData, optimizedParams, initialBalance)
        
        // Save backtest result
        backtestDao.insert(result.toEntity())
        
        return result
    }
    
    private suspend fun optimizeParameters(candles: List<CandleData>): StrategyParameters {
        // Define parameter ranges to test
        val emaShortRange = listOf(10, 15, 20)
        val emaLongRange = listOf(50, 100, 200)
        val rsiPeriodRange = listOf(12, 14, 16)
        val rsiBuyRange = 40..50 step 2
        val rsiSellRange = 60..70 step 2
        
        var bestParams = StrategyParameters()
        var bestScore = Double.MIN_VALUE
        
        // Grid search optimization
        emaShortRange.forEach { emaShort ->
            emaLongRange.forEach { emaLong ->
                rsiPeriodRange.forEach { rsiPeriod ->
                    rsiBuyRange.forEach { rsiBuy ->
                        rsiSellRange.forEach { rsiSell ->
                            val params = StrategyParameters(
                                emaShortPeriod = emaShort,
                                emaLongPeriod = emaLong,
                                rsiPeriod = rsiPeriod,
                                rsiBuyThreshold = rsiBuy,
                                rsiSellThreshold = rsiSell
                            )
                            
                            val tempResult = executeBacktest(candles, params, 10000.0)
                            val score = calculateScore(tempResult)
                            
                            if (score > bestScore) {
                                bestScore = score
                                bestParams = params
                            }
                        }
                    }
                }
            }
        }
        
        return bestParams
    }
    
    private suspend fun executeBacktest(
        candles: List<CandleData>,
        params: StrategyParameters,
        initialBalance: Double
    ): BacktestResult {
        var balance = initialBalance
        var position = 0.0
        var entryPrice = 0.0
        val trades = mutableListOf<Trade>()
        
        // Convert candles to 15m, 1H, 4H timeframes for multi-timeframe analysis
        val timeframeConverter = TimeframeConverter()
        val candles15m = timeframeConverter.convert(candles, "15m")
        val candles1H = timeframeConverter.convert(candles, "1h")
        val candles4H = timeframeConverter.convert(candles, "4h")
        
        // Simulate strategy execution
        for (i in params.emaLongPeriod until candles15m.size) {
            val currentCandle = candles15m[i]
            val currentPrice = currentCandle.close
            
            // Get slices for current time
            val slice15m = candles15m.subList(0, i + 1)
            val slice1H = candles1H.takeWhile { it.time <= currentCandle.time }
            val slice4H = candles4H.takeWhile { it.time <= currentCandle.time }
            
            // Calculate indicators
            val indicators15m = strategyEngine.calculateIndicators(slice15m, params)
            val indicators1H = strategyEngine.calculateIndicators(slice1H, params)
            val indicators4H = strategyEngine.calculateIndicators(slice4H, params)
            
            // Generate signals
            val signal = strategyEngine.generateSignal(
                currentPrice = currentPrice,
                indicators15m = indicators15m,
                indicators1H = indicators1H,
                indicators4H = indicators4H,
                position = position
            )
            
            // Execute trades
            when (signal) {
                is Signal.Buy -> {
                    val amount = balance / currentPrice
                    position = amount
                    entryPrice = currentPrice
                    balance = 0.0
                    
                    trades.add(Trade(
                        timestamp = currentCandle.time,
                        type = TradeType.BUY,
                        price = currentPrice,
                        amount = amount
                    ))
                }
                is Signal.Sell -> {
                    balance = position * currentPrice
                    val profit = balance - (position * entryPrice)
                    
                    trades.add(Trade(
                        timestamp = currentCandle.time,
                        type = TradeType.SELL,
                        price = currentPrice,
                        amount = position,
                        profit = profit
                    ))
                    
                    position = 0.0
                }
                Signal.Hold -> {
                    // No action
                }
            }
        }
        
        // Calculate final balance (including any open position)
        val finalBalance = balance + (position * candles15m.last().close)
        
        return BacktestResult(
            symbol = "BTCUSDT", // Example
            timeframe = "15m",
            startTime = candles15m.first().time,
            endTime = candles15m.last().time,
            initialBalance = initialBalance,
            finalBalance = finalBalance,
            profit = finalBalance - initialBalance,
            returnPercentage = ((finalBalance - initialBalance) / initialBalance) * 100,
            totalTrades = trades.size,
            winningTrades = trades.count { it.profit ?: 0.0 > 0 },
            losingTrades = trades.count { it.profit ?: 0.0 < 0 },
            maxDrawdown = calculateMaxDrawdown(trades, initialBalance),
            parameters = params,
            trades = trades
        )
    }
    
    private fun calculateScore(result: BacktestResult): Double {
        // Custom scoring function that considers profit, drawdown, win rate, etc.
        val profitScore = result.returnPercentage * 0.5
        val winRateScore = (result.winningTrades.toDouble() / result.totalTrades) * 30
        val drawdownPenalty = result.maxDrawdown * -20
        
        return profitScore + winRateScore + drawdownPenalty
    }
    
    private fun calculateMaxDrawdown(trades: List<Trade>, initialBalance: Double): Double {
        var peak = initialBalance
        var maxDrawdown = 0.0
        var currentBalance = initialBalance
        
        trades.forEach { trade ->
            currentBalance += trade.profit ?: 0.0
            if (currentBalance > peak) {
                peak = currentBalance
            }
            val drawdown = (peak - currentBalance) / peak * 100
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown
            }
        }
        
        return maxDrawdown
    }
}

data class StrategyParameters(
    val emaShortPeriod: Int = 20,
    val emaLongPeriod: Int = 50,
    val rsiPeriod: Int = 14,
    val rsiBuyThreshold: Int = 40,
    val rsiSellThreshold: Int = 60,
    val macdFastPeriod: Int = 12,
    val macdSlowPeriod: Int = 26,
    val macdSignalPeriod: Int = 9
)

sealed class Signal {
    object Buy : Signal()
    object Sell : Signal()
    object Hold : Signal()
}

data class BacktestResult(
    val symbol: String,
    val timeframe: String,
    val startTime: Long,
    val endTime: Long,
    val initialBalance: Double,
    val finalBalance: Double,
    val profit: Double,
    val returnPercentage: Double,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val maxDrawdown: Double,
    val parameters: StrategyParameters,
    val trades: List<Trade>
)

data class Trade(
    val timestamp: Long,
    val type: TradeType,
    val price: Double,
    val amount: Double,
    val profit: Double? = null
)

enum class TradeType { BUY, SELL }