interface BinanceApiService {
    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 500
    ): List<List<String>>
    
    @GET("api/v3/ticker/24hr")
    suspend fun get24hrTicker(
        @Query("symbol") symbol: String
    ): BinanceTickerResponse
    
    @GET("api/v3/exchangeInfo")
    suspend fun getExchangeInfo(): BinanceExchangeInfo
    
    @GET("api/v3/ticker/price")
    suspend fun getAllPrices(): List<BinancePriceResponse>
}

data class BinanceTickerResponse(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("priceChangePercent") val priceChangePercent: String,
    @SerializedName("lastPrice") val lastPrice: String,
    @SerializedName("volume") val volume: String,
    @SerializedName("quoteVolume") val quoteVolume: String
)

data class BinanceExchangeInfo(
    @SerializedName("symbols") val symbols: List<BinanceSymbol>
)

data class BinanceSymbol(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("status") val status: String,
    @SerializedName("baseAsset") val baseAsset: String,
    @SerializedName("quoteAsset") val quoteAsset: String
)

data class BinancePriceResponse(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("price") val price: String
)