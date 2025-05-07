@HiltAndroidTest
class StrategyEngineTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var strategyEngine: StrategyEngine
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun testTrendAnalysis_bullish() {
        // Create bullish candle data (rising prices)
        val candles = List(100) { i ->
            CandleData(
                open = 100.0 + i,
                high = 105.0 + i,
                low = 95.0 + i,
                close = 102.0 + i,
                volume = 1000.0,
                time = System.currentTimeMillis() - (100 - i) * 900000 // 15m intervals
            )
        }
        
        val indicators = strategyEngine.calculateAll(candles)
        val trendAnalysis = strategyEngine.analyzeTrend(
            indicators15m = indicators,
            indicators1H = indicators,
            indicators4H = indicators
        )
        
        assertTrue(trendAnalysis.emaAlignment.allAligned)
        assertEquals(TrendDirection.UPTREND, trendAnalysis.trendDirection)
    }
    
    @Test
    fun testPullbackAnalysis() {
        // Create pullback scenario
        val basePrice = 100.0
        val candles = List(50) { i ->
            val pullbackFactor = if (i in 30..40) 0.95 else 1.0
            CandleData(
                open = basePrice * (1 + i * 0.01) * pullbackFactor,
                high = basePrice * (1 + i * 0.01) * pullbackFactor * 1.02,
                low = basePrice * (1 + i * 0.01) * pullbackFactor * 0.98,
                close = basePrice * (1 + i * 0.01) * pullbackFactor,
                volume = 1000.0,
                time = System.currentTimeMillis() - (50 - i) * 3600000 // 1H intervals
            )
        }
        
        val indicators = strategyEngine.calculateAll(candles)
        val pullbackAnalysis = strategyEngine.analyzePullback(
            indicators15m = indicators,
            indicators1H = indicators,
            candles15m = candles,
            candles1H = candles
        )
        
        assertTrue(pullbackAnalysis.inPullbackZone)
    }
    
    @Test
    fun testMomentumAnalysis() {
        // Create momentum scenario
        val candles = List(100) { i ->
            val rsiFactor = if (i < 70) 0.5 else 1.5 // Simulate increasing momentum
            CandleData(
                open = 100.0,
                high = 102.0,
                low = 98.0,
                close = 100.0 + (i * rsiFactor),
                volume = 1000.0,
                time = System.currentTimeMillis() - (100 - i) * 900000 // 15m intervals
            )
        }
        
        val indicators = strategyEngine.calculateAll(candles)
        val momentumAnalysis = strategyEngine.analyzeMomentum(
            indicators15m = indicators,
            indicators1H = indicators
        )
        
        assertTrue(momentumAnalysis.momentumConfirmation)
        assertTrue(momentumAnalysis.rsiConditions.rsi15mAbove50)
        assertTrue(momentumAnalysis.macdConditions.macdAboveSignal)
    }
}