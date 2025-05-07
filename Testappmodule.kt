@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {
    @Provides
    @Singleton
    fun provideMockBinanceRepository(): BinanceRepository {
        return mockk<BinanceRepository>(relaxed = true) {
            every { getCandleData(any(), any(), any()) } returns emptyList()
            every { getVolumeData(any(), any(), any()) } returns VolumeData("", emptyList(), emptyList())
            every { getAllCoins() } returns listOf(
                Coin("BTCUSDT", "Bitcoin", 50000.0, 5.0, 1_000_000_000.0, ""),
                Coin("ETHUSDT", "Ethereum", 3000.0, 2.5, 500_000_000.0, "")
            )
        }
    }
    
    @Provides
    @Singleton
    fun provideMockCoinDao(): CoinDao {
        return mockk<CoinDao>(relaxed = true)
    }
    
    @Provides
    @Singleton
    fun provideMockBacktestDao(): BacktestDao {
        return mockk<BacktestDao>(relaxed = true)
    }
    
    @Provides
    @Singleton
    fun provideMockWebSocketManager(): WebSocketManager {
        return mockk<WebSocketManager>(relaxed = true)
    }
}