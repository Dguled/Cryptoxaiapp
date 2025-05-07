class NetworkOptimizer @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val connectivityManager: ConnectivityManager,
    private val binanceRepository: BinanceRepository
) {
    private var currentNetworkType: String = ""
    
    fun optimizeNetworkCalls() {
        val networkInfo = connectivityManager.activeNetworkInfo
        
        currentNetworkType = when {
            networkInfo == null -> "DISCONNECTED"
            networkInfo.type == ConnectivityManager.TYPE_WIFI -> "WIFI"
            networkInfo.type == ConnectivityManager.TYPE_MOBILE -> {
                when (networkInfo.subtype) {
                    TelephonyManager.NETWORK_TYPE_LTE,
                    TelephonyManager.NETWORK_TYPE_NR -> "MOBILE_FAST"
                    else -> "MOBILE_SLOW"
                }
            }
            else -> "OTHER"
        }
        
        adjustApiParameters()
    }
    
    private fun adjustApiParameters() {
        when (currentNetworkType) {
            "WIFI" -> {
                // Use aggressive settings for WiFi
                binanceRepository.setRequestLimit(500)
                binanceRepository.setWebSocketReconnectInterval(0) // immediate
            }
            "MOBILE_FAST" -> {
                // Moderate settings for fast mobile
                binanceRepository.setRequestLimit(200)
                binanceRepository.setWebSocketReconnectInterval(5000) // 5 seconds
            }
            "MOBILE_SLOW" -> {
                // Conservative settings for slow connections
                binanceRepository.setRequestLimit(100)
                binanceRepository.setWebSocketReconnectInterval(10000) // 10 seconds
            }
            else -> {
                // Default settings
                binanceRepository.setRequestLimit(200)
                binanceRepository.setWebSocketReconnectInterval(5000)
            }
        }
    }
    
    fun shouldFetchData(): Boolean {
        return when (currentNetworkType) {
            "DISCONNECTED" -> false
            "MOBILE_SLOW" -> {
                // Only fetch if battery is not low
                val batteryIntent = PowerManager().getBatteryProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                batteryIntent ?: 0 > 20
            }
            else -> true
        }
    }
}