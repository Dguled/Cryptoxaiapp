class PerformanceMonitor @Inject constructor(
    private val activityManager: ActivityManager,
    private val powerManager: PowerManager,
    private val connectivityManager: ConnectivityManager
) {
    private val stats = mutableMapOf<String, PerformanceStat>()
    private var startTime: Long = 0
    
    fun startMonitoring(tag: String) {
        startTime = System.currentTimeMillis()
        stats[tag] = PerformanceStat(
            startTime = startTime,
            initialMemory = getUsedMemory(),
            initialBattery = getBatteryLevel(),
            networkState = getNetworkState()
        )
    }
    
    fun endMonitoring(tag: String) {
        val endTime = System.currentTimeMillis()
        stats[tag] = stats[tag]?.copy(
            endTime = endTime,
            duration = endTime - startTime,
            finalMemory = getUsedMemory(),
            finalBattery = getBatteryLevel(),
            memoryUsed = getUsedMemory() - (stats[tag]?.initialMemory ?: 0)
        )
    }
    
    fun getStats(tag: String): PerformanceStat? {
        return stats[tag]
    }
    
    fun logAllStats() {
        stats.forEach { (tag, stat) ->
            Log.d("PerformanceMonitor", 
                """
                |Performance Stat [$tag]
                |Duration: ${stat.duration}ms
                |Memory Used: ${stat.memoryUsed / 1024}KB
                |Battery Drain: ${stat.finalBattery - stat.initialBattery}%
                |Network: ${stat.networkState}
                """.trimMargin())
        }
    }
    
    private fun getUsedMemory(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }
    
    private fun getBatteryLevel(): Int {
        val batteryIntent = powerManager.getBatteryProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return batteryIntent ?: -1
    }
    
    private fun getNetworkState(): String {
        val networkInfo = connectivityManager.activeNetworkInfo
        return when {
            networkInfo == null -> "Disconnected"
            networkInfo.type == ConnectivityManager.TYPE_WIFI -> "WiFi"
            networkInfo.type == ConnectivityManager.TYPE_MOBILE -> "Mobile"
            else -> "Other"
        }
    }
}

data class PerformanceStat(
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Long? = null,
    val initialMemory: Long,
    val finalMemory: Long? = null,
    val memoryUsed: Long? = null,
    val initialBattery: Int,
    val finalBattery: Int? = null,
    val networkState: String
)