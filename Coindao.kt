@Dao
interface CoinDao {
    @Query("SELECT * FROM coins ORDER BY marketCap DESC")
    suspend fun getAllCoins(): List<CoinEntity>
    
    @Query("SELECT * FROM coins WHERE isInWatchlist = 1 ORDER BY lastUpdated DESC")
    suspend fun getWatchlistCoins(): List<CoinEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coins: List<CoinEntity>)
    
    @Update
    suspend fun updateCoin(coin: CoinEntity)
    
    @Query("UPDATE coins SET isInWatchlist = :inWatchlist WHERE symbol = :symbol")
    suspend fun updateWatchlistStatus(symbol: String, inWatchlist: Boolean)
    
    @Query("SELECT * FROM analysis_results WHERE symbol = :symbol ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAnalysis(symbol: String): AnalysisResultEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysisResult(result: AnalysisResultEntity)
}