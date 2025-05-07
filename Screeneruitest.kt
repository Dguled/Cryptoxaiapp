@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ScreenerUITest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Inject
    lateinit var repository: BinanceRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun testScreenerListDisplay() {
        // Mock data
        val testCoins = listOf(
            Coin(
                symbol = "BTCUSDT",
                name = "Bitcoin",
                price = 50000.0,
                change24h = 5.0,
                marketCap = 1_000_000_000.0,
                iconUrl = ""
            ),
            Coin(
                symbol = "ETHUSDT",
                name = "Ethereum",
                price = 3000.0,
                change24h = 2.5,
                marketCap = 500_000_000.0,
                iconUrl = ""
            )
        )
        
        // Set up fake repository
        val fakeRepository = object : BinanceRepository {
            override suspend fun getCandleData(symbol: String, interval: String, limit: Int) = emptyList()
            override suspend fun getVolumeData(symbol: String, interval: String, limit: Int) = VolumeData(symbol, emptyList(), emptyList())
            override suspend fun getAllCoins() = testCoins
            override fun subscribeToRealTimeUpdates(symbols: List<String>, onUpdate: (String, CandleData) -> Unit) = Disposable {}
        }
        
        composeTestRule.setContent {
            CryptoXAiTheme {
                ScreenerScreen(
                    viewModel = ScreenerViewModel(fakeRepository, StrategyEngine(), CoinRepositoryImpl())
                )
            }
        }
        
        // Verify coins are displayed
        composeTestRule.onNodeWithText("BTCUSDT").assertIsDisplayed()
        composeTestRule.onNodeWithText("ETHUSDT").assertIsDisplayed()
        
        // Verify price changes are colored correctly
        composeTestRule.onAllNodesWithTag("priceChange")[0]
            .assert(hasTextColor(Color.Green))
        composeTestRule.onAllNodesWithTag("priceChange")[1]
            .assert(hasTextColor(Color.Green))
    }
    
    @Test
    fun testBacktestFunctionality() {
        val fakeBacktestResult = BacktestResult(
            symbol = "BTCUSDT",
            timeframe = "15m",
            startTime = System.currentTimeMillis() - 86400000,
            endTime = System.currentTimeMillis(),
            initialBalance = 10000.0,
            finalBalance = 11500.0,
            profit = 1500.0,
            returnPercentage = 15.0,
            totalTrades = 10,
            winningTrades = 7,
            losingTrades = 3,
            maxDrawdown = 5.0,
            parameters = StrategyParameters(),
            trades = emptyList()
        )
        
        composeTestRule.setContent {
            CryptoXAiTheme {
                BacktestResultScreen(
                    result = fakeBacktestResult,
                    onBack = {}
                )
            }
        }
        
        // Verify backtest results are displayed correctly
        composeTestRule.onNodeWithText("15.0%").assertIsDisplayed()
        composeTestRule.onNodeWithText("70% win rate").assertIsDisplayed()
        composeTestRule.onNodeWithText("5.0% max drawdown").assertIsDisplayed()
    }
}

fun hasTextColor(color: Color): SemanticsMatcher {
    return SemanticsMatcher.expectValue(
        SemanticsProperties.Color,
        color
    )
}