@Entity(tableName = "coins")
data class CoinEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val price: Double,
    val change24h: Double,
    val marketCap: Double,
    val iconUrl: String,
    val isInWatchlist: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun CoinEntity.toCoin() = Coin(
    symbol = symbol,
    name = name,
    price = price,
    change24h = change24h,
    marketCap = marketCap,
    iconUrl = iconUrl
)

fun Coin.toCoinEntity() = CoinEntity(
    symbol = symbol,
    name = name,
    price = price,
    change24h = change24h,
    marketCap = marketCap,
    iconUrl = iconUrl
)

@Entity(tableName = "analysis_results")
data class AnalysisResultEntity(
    @PrimaryKey val symbol: String,
    val resultJson: String,
    val timestamp: Long
)