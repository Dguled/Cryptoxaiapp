class DataCacheManager @Inject constructor(
    private val binanceRepository: BinanceRepository,
    private val coinDao: CoinDao,
    private val backtestDao: BacktestDao
) {
    private val cache = mutableMapOf<String, CacheEntry>()
    private val maxCacheSize = 50 // Max items in memory cache
    
    suspend fun getCachedCandles(
        symbol: String,
        interval: String,
        limit: Int
    ): List<CandleData> {
        val cacheKey = "$symbol-$interval-$limit"
        
        // Check memory cache first
        cache[cacheKey]?.let {
            if (System.currentTimeMillis() - it.timestamp < CACHE_EXPIRY) {
                return it.data
            }
        }
        
        // Check database cache
        val dbCache = coinDao.getCachedCandles(symbol, interval, limit)
        if (dbCache.isNotEmpty()) {
            // Update memory cache
            cache[cacheKey] = CacheEntry(dbCache, System.currentTimeMillis())
            ensureCacheSize()
            return dbCache
        }
        
        // Fetch from API if no cache available
        val freshData = binanceRepository.getCandleData(symbol, interval, limit)
        
        // Update caches
        cache[cacheKey] = CacheEntry(freshData, System.currentTimeMillis())
        ensureCacheSize()
        
        // Save to database in background
        viewModelScope.launch {
            coinDao.insertCandles(freshData.map { it.toCandleEntity(symbol, interval) })
        }
        
        return freshData
    }
    
    suspend fun getCachedBacktestResults(): List<BacktestResult> {
        return backtestDao.getAll().map { it.toBacktestResult() }
    }
    
    private fun ensureCacheSize() {
        if (cache.size > maxCacheSize) {
            // Remove oldest entries
            val sorted = cache.entries.sortedBy { it.value.timestamp }
            val toRemove = sorted.take(cache.size - maxCacheSize)
            toRemove.forEach { cache.remove(it.key) }
        }
    }
    
    companion object {
        private const val CACHE_EXPIRY = 30 * 60 * 1000 // 30 minutes
    }
}

data class CacheEntry(
    val data: List<CandleData>,
    val timestamp: Long
)

@Entity(tableName = "cached_candles")
data class CandleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val symbol: String,
    val interval: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val time: Long,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CandleData.toCandleEntity(symbol: String, interval: String): CandleEntity {
    return CandleEntity(
        symbol = symbol,
        interval = interval,
        open = open,
        high = high,
        low = low,
        close = close,
        volume = volume,
        time = time
    )
}