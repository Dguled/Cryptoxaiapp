class PriceActionAnalyzer @Inject constructor() {
    
    fun detectSignals(
        candles15m: List<CandleData>,
        candles1H: List<CandleData>,
        indicators15m: Indicators,
        indicators1H: Indicators
    ): List<PriceActionSignal> {
        val signals = mutableListOf<PriceActionSignal>()
        
        // Analyze last 3 candles for patterns
        val recent15m = candles15m.takeLast(3)
        val recent1H = candles1H.takeLast(3)
        
        // Check for bullish engulfing
        if (isBullishEngulfing(recent15m)) {
            signals.add(PriceActionSignal.BullishEngulfing(TimeFrame.M15))
        }
        
        if (isBullishEngulfing(recent1H)) {
            signals.add(PriceActionSignal.BullishEngulfing(TimeFrame.H1))
        }
        
        // Check for morning star
        if (isMorningStar(recent15m)) {
            signals.add(PriceActionSignal.MorningStar(TimeFrame.M15))
        }
        
        if (isMorningStar(recent1H)) {
            signals.add(PriceActionSignal.MorningStar(TimeFrame.H1))
        }
        
        // Check for hammer
        if (isHammer(recent15m.last(), indicators15m.ema50)) {
            signals.add(PriceActionSignal.Hammer(TimeFrame.M15))
        }
        
        if (isHammer(recent1H.last(), indicators1H.ema50)) {
            signals.add(PriceActionSignal.Hammer(TimeFrame.H1))
        }
        
        // Check for breakout above EMA50
        if (isBreakoutAboveEMA50(candles15m, indicators15m.ema50)) {
            signals.add(PriceActionSignal.BreakoutAboveEMA50(TimeFrame.M15))
        }
        
        if (isBreakoutAboveEMA50(candles1H, indicators1H.ema50)) {
            signals.add(PriceActionSignal.BreakoutAboveEMA50(TimeFrame.H1))
        }
        
        return signals
    }
    
    private fun isBullishEngulfing(candles: List<CandleData>): Boolean {
        if (candles.size < 2) return false
        
        val prev = candles[0]
        val current = candles[1]
        
        return prev.close < prev.open &&  // Previous candle is bearish
               current.open < prev.close &&  // Current opens below previous close
               current.close > prev.open &&  // Current closes above previous open
               current.close > current.open  // Current candle is bullish
    }
    
    private fun isMorningStar(candles: List<CandleData>): Boolean {
        if (candles.size < 3) return false
        
        val first = candles[0]
        val second = candles[1]
        val third = candles[2]
        
        return first.close < first.open &&  // First candle is bearish
               second.open < first.close &&  // Second candle gaps down
               second.close < second.open &&  // Second candle is bearish
               third.open > second.close &&  // Third candle gaps up
               third.close > third.open &&  // Third candle is bullish
               third.close > first.close  // Closes above first candle's close
    }
    
    private fun isHammer(candle: CandleData, ema50: Double): Boolean {
        val bodySize = abs(candle.open - candle.close)
        val lowerShadow = if (candle.open > candle.close) {
            candle.close - candle.low
        } else {
            candle.open - candle.low
        }
        val upperShadow = if (candle.open > candle.close) {
            candle.high - candle.open
        } else {
            candle.high - candle.close
        }
        
        return lowerShadow >= 2 * bodySize &&  // Long lower shadow
               upperShadow <= bodySize * 0.5 &&  // Small or no upper shadow
               candle.close > ema50  // Closing above EMA50 for confirmation
    }
    
    private fun isBreakoutAboveEMA50(
        candles: List<CandleData>,
        ema50: Double
    ): Boolean {
        if (candles.size < 3) return false
        
        // Check if previous candles were below EMA50
        val previousBelow = candles.takeLast(3).all { 
            it.high < ema50 
        }
        
        // Current candle breaks above EMA50
        val currentBreak = candles.last().close > ema50 && 
                          candles.last().high > ema50 * 1.005  // With some margin
        
        return previousBelow && currentBreak
    }
}

sealed class PriceActionSignal(val timeframe: TimeFrame) {
    class BullishEngulfing(timeframe: TimeFrame) : PriceActionSignal(timeframe)
    class MorningStar(timeframe: TimeFrame) : PriceActionSignal(timeframe)
    class Hammer(timeframe: TimeFrame) : PriceActionSignal(timeframe)
    class BreakoutAboveEMA50(timeframe: TimeFrame) : PriceActionSignal(timeframe)
}

enum class TimeFrame {
    M15, H1, H4
}