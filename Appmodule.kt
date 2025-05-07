@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBinanceApiService(okHttpClient: OkHttpClient): BinanceApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BinanceApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideWebSocketManager(okHttpClient: OkHttpClient): WebSocketManager {
        return WebSocketManager(okHttpClient)
    }
    
    @Provides
    @Singleton
    fun provideBinanceRepository(
        apiService: BinanceApiService,
        webSocketManager: WebSocketManager,
        coinDao: CoinDao
    ): BinanceRepository {
        return BinanceRepositoryImpl(apiService, webSocketManager, coinDao)
    }
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideCoinDao(database: AppDatabase): CoinDao {
        return database.coinDao()
    }
    
    @Provides
    @Singleton
    fun provideIndicatorCalculator(): IndicatorCalculator {
        return IndicatorCalculator()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    @Provides
    fun provideCoinRepository(coinDao: CoinDao): CoinRepository {
        return CoinRepositoryImpl(coinDao)
    }
}