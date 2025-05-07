class BatteryOptimizer @Inject constructor(
    private val powerManager: PowerManager,
    private val workManager: WorkManager
) {
    private var isBatteryLow = false
    private var lastOptimizationTime = 0L
    
    fun checkBatteryState() {
        val batteryStatus = powerManager.getBatteryProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        val batteryLevel = powerManager.getBatteryProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        
        isBatteryLow = batteryLevel ?: 100 <= 20
        
        if (isBatteryLow) {
            optimizeForLowBattery()
        } else if (System.currentTimeMillis() - lastOptimizationTime > 3600000) { // 1 hour
            resetOptimizations()
        }
    }
    
    private fun optimizeForLowBattery() {
        lastOptimizationTime = System.currentTimeMillis()
        
        // Reduce background work frequency
        workManager.cancelAllWork()
        
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<CoinSyncWorker>(
            12, TimeUnit.HOURS) // Reduced frequency
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "coin_sync_work",
            ExistingPeriodicWorkPolicy.REPLACE,
            syncRequest
        )
        
        // Disable real-time updates
        BinanceRealTimeManager.disableUpdates()
    }
    
    private fun resetOptimizations() {
        // Restore normal work frequency
        workManager.cancelAllWork()
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<CoinSyncWorker>(
            4, TimeUnit.HOURS) // Normal frequency
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "coin_sync_work",
            ExistingPeriodicWorkPolicy.REPLACE,
            syncRequest
        )
        
        // Enable real-time updates
        BinanceRealTimeManager.enableUpdates()
    }
}