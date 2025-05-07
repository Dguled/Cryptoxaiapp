interface BacktestRepository {
    suspend fun runBacktest(
        symbol: String,
        startTime: Long,
        endTime: Long,
        timeframe: String,
        initialBalance: Double
    ): BacktestResult
    
    suspend fun getBacktestResults(): List<BacktestResult>
    suspend fun getBacktestResult(id: String): BacktestResult?
    suspend fun deleteBacktestResult(id: String)
}

class BacktestRepositoryImpl @Inject constructor(
    private val backtestEngine: BacktestEngine,
    private val backtestDao: BacktestDao
) : BacktestRepository {
    
    override suspend fun runBacktest(
        symbol: String,
        startTime: Long,
        endTime: Long,
        timeframe: String,
        initialBalance: Double
    ): BacktestResult {
        return backtestEngine.runBacktest(symbol, startTime, endTime, timeframe, initialBalance)
    }
    
    override suspend fun getBacktestResults(): List<BacktestResult> {
        return backtestDao.getAll().map { it.toBacktestResult() }
    }
    
    override suspend fun getBacktestResult(id: String): BacktestResult? {
        return backtestDao.getById(id)?.toBacktestResult()
    }
    
    override suspend fun deleteBacktestResult(id: String) {
        backtestDao.delete(id)
    }
}