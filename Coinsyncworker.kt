class CoinSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val binanceRepository: BinanceRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // Fetch all coins data
            binanceRepository.getAllCoins()
            
            // If we reach here, the sync was successful
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    @AssistedFactory
    interface Factory : ChildWorkerFactory
    
    companion object {
        const val TAG = "CoinSyncWorker"
    }
}

interface ChildWorkerFactory {
    fun create(
        context: Context,
        params: WorkerParameters
    ): ListenableWorker
}

@Module
@InstallIn(WorkerFactoryModule::class)
object WorkerBindingModule {
    @Provides
    fun bindCoinSyncWorkerFactory(factory: CoinSyncWorker.Factory): ChildWorkerFactory = factory
}