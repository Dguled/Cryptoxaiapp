class BatteryAwareFetcher @Inject constructor(
    private val powerManager: PowerManager,
    private val networkOptimizer: NetworkOptimizer,
    private val binanceRepository: BinanceRepository
) {
    suspend fun fetchDataWithBatteryAwareness(
        symbol: String,
        interval: String,
        limit: Int
    ): List<CandleData> {
        val batteryLevel = powerManager.getBatteryProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        
        return when {
            batteryLevel == null -> {
                // Battery info not available - proceed normally
                binanceRepository.getCandleData(symbol, interval, limit)
            }
            batteryLevel < 15 -> {
                // Critical battery - return empty or cached data only
                emptyList()
            }
            batteryLevel < 30 -> {
                // Low battery - reduce data size
                binanceRepository.getCandleData(symbol, interval, min(limit, 50))
            }
            else -> {
                // Normal battery - check network conditions first
                if (networkOptimizer.shouldFetchData()) {
                    binanceRepository.getCandleData(symbol, interval, limit)
                } else {
                    emptyList()
                }
            }
        }
    }
}