@RunWith(AndroidJUnit4::class)
class PerformanceTest {
    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java)
    
    @Test
    fun testStrategyEnginePerformance() {
        // Generate test data
        val candles = List(1000) { i ->
            CandleData(
                open = 100.0 + (i % 100),
                high = 105.0 + (i % 100),
                low = 95.0 + (i % 100),
                close = 102.0 + (i % 100),
                volume = 1000.0 + (i * 10),
                time = System.currentTimeMillis() - (1000 - i) * 900000
            )
        }
        
        // Measure execution time
        val startTime = System.currentTimeMillis()
        val strategyEngine = StrategyEngine()
        strategyEngine.analyzeCoin(
            symbol = "BTCUSDT",
            candles15m = candles,
            candles1H = candles,
            candles4H = candles,
            volumeData = VolumeData("BTCUSDT", candles.map { it.volume }, candles.map { it.time })
        )
        val duration = System.currentTimeMillis() - startTime
        
        // Assert performance threshold (should complete in < 500ms)
        assertTrue("Strategy analysis took too long: ${duration}ms", duration < 500)
    }
    
    @Test
    fun testMemoryUsage() {
        val activityManager = rule.activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val initialMemory = activityManager.memoryInfo.availMem
        
        // Load large dataset
        val largeDataset = List(10000) { i ->
            CandleData(
                open = 100.0 + (i % 100),
                high = 105.0 + (i % 100),
                low = 95.0 + (i % 100),
                close = 102.0 + (i % 100),
                volume = 1000.0 + (i * 10),
                time = System.currentTimeMillis() - (10000 - i) * 900000
            )
        }
        
        val finalMemory = activityManager.memoryInfo.availMem
        val memoryUsed = initialMemory - finalMemory
        
        // Assert memory threshold (should use < 50MB)
        assertTrue("Memory usage too high: ${memoryUsed / (1024 * 1024)}MB", 
            memoryUsed < 50 * 1024 * 1024)
    }
}