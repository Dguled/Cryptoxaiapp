class BinanceRepositoryImpl @Inject constructor(
    private val apiService: BinanceApiService,
    private val webSocketManager: WebSocketManager,
    private val coinDao: CoinDao
) : BinanceRepository {
    
    override suspend fun getCandleData(
        symbol: String,
        interval: String,
        limit: Int
    ): List<CandleData> {
        val response = apiService.getKlines(
            symbol = symbol,
            interval = interval,
            limit = limit
        )
        
        return response.mapNotNull { kline ->
            try {
                CandleData(
                    open = kline[1].toDouble(),
                    high = kline[2].toDouble(),
                    low = kline[3].toDouble(),
                    close = kline[4].toDouble(),
                    volume = kline[5].toDouble(),
                    time = kline[0].toLong()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun getVolumeData(
        symbol: String,
        interval: String,
        limit: Int
    ): VolumeData {
        val candles = getCandleData(symbol, interval, limit)
        return VolumeData(
            symbol = symbol,
            values = candles.map { it.volume },
            times = candles.map { it.time }
        )
    }
    
    override suspend fun getAllCoins(): List<Coin> {
        // First try to get from local DB
        val localCoins = coinDao.getAllCoins()
        if (localCoins.isNotEmpty()) {
            return localCoins.map { it.toCoin() }
        }
        
        // Fetch from API if local DB is empty
        val exchangeInfo = apiService.getExchangeInfo()
        val prices = apiService.getAllPrices()
        val tickers = exchangeInfo.symbols.mapNotNull { symbol ->
            try {
                apiService.get24hrTicker(symbol.symbol)
            } catch (e: Exception) {
                null
            }
        }
        
        val coins = exchangeInfo.symbols.mapNotNull { symbol ->
            val priceResponse = prices.find { it.symbol == symbol.symbol }
            val ticker = tickers.find { it.symbol == symbol.symbol }
            
            if (priceResponse != null && ticker != null) {
                Coin(
                    symbol = symbol.symbol,
                    name = symbol.baseAsset,
                    price = priceResponse.price.toDouble(),
                    change24h = ticker.priceChangePercent.toDouble(),
                    marketCap = ticker.quoteVolume.toDouble(),
                    iconUrl = "https://cryptoicon-api.vercel.app/api/icon/${symbol.baseAsset.lowercase()}"
                )
            } else null
        }
        
        // Save to local DB
        coinDao.insertAll(coins.map { it.toCoinEntity() })
        
        return coins
    }
    
    override fun subscribeToRealTimeUpdates(
        symbols: List<String>,
        onUpdate: (String, CandleData) -> Unit
    ): Disposable {
        return webSocketManager.subscribeToKlines(symbols, "15m", onUpdate)
    }
}