@Dao
interface BacktestDao {
    @Query("SELECT * FROM backtest_results ORDER BY endTime DESC")
    suspend fun getAll(): List<BacktestResultEntity>
    
    @Query("SELECT * FROM backtest_results WHERE id = :id")
    suspend fun getById(id: String): BacktestResultEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: BacktestResultEntity)
    
    @Delete
    suspend fun delete(result: BacktestResultEntity)
    
    @Query("DELETE FROM backtest_results WHERE id = :id")
    suspend fun delete(id: String)
}

@Entity(tableName = "backtest_results")
data class BacktestResultEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val symbol: String,
    val timeframe: String,
    val startTime: Long,
    val endTime: Long,
    val initialBalance: Double,
    val finalBalance: Double,
    val profit: Double,
    val returnPercentage: Double,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val maxDrawdown: Double,
    val parametersJson: String,
    val tradesJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun BacktestResultEntity.toBacktestResult(): BacktestResult {
    val gson = Gson()
    return BacktestResult(
        symbol = symbol,
        timeframe = timeframe,
        startTime = startTime,
        endTime = endTime,
        initialBalance = initialBalance,
        finalBalance = finalBalance,
        profit = profit,
        returnPercentage = returnPercentage,
        totalTrades = totalTrades,
        winningTrades = winningTrades,
        losingTrades = losingTrades,
        maxDrawdown = maxDrawdown,
        parameters = gson.fromJson(parametersJson, StrategyParameters::class.java),
        trades = gson.fromJson(tradesJson, object : TypeToken<List<Trade>>() {}.type)
    )
}

fun BacktestResult.toEntity(): BacktestResultEntity {
    val gson = Gson()
    return BacktestResultEntity(
        symbol = symbol,
        timeframe = timeframe,
        startTime = startTime,
        endTime = endTime,
        initialBalance = initialBalance,
        finalBalance = finalBalance,
        profit = profit,
        returnPercentage = returnPercentage,
        totalTrades = totalTrades,
        winningTrades = winningTrades,
        losingTrades = losingTrades,
        maxDrawdown = maxDrawdown,
        parametersJson = gson.toJson(parameters),
        tradesJson = gson.toJson(trades)
    )
}