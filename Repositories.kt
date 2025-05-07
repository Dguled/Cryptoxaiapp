interface BinanceRepository {
    suspend fun getCandleData(
        symbol: String,
        interval: String,
        limit: Int
    ): List<CandleData>
    
    suspend fun getVolumeData(
        symbol: String,
        interval: String,
        limit: Int
    ): VolumeData
    
    suspend fun getAllCoins(): List<Coin>
    
    fun subscribeToRealTimeUpdates(
        symbols: List<String>,
        onUpdate: (String, CandleData) -> Unit
    ): Disposable
}

interface CoinRepository {
    suspend fun getWatchlistCoins(): List<Coin>
    suspend fun addToWatchlist(symbol: String)
    suspend fun removeFromWatchlist(symbol: String)
    fun observeWatchlistChanges(): Flow<List<Coin>>
}