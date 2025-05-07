class WebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val webSockets = mutableMapOf<String, WebSocket>()
    private val subscriptions = mutableMapOf<String, MutableList<(CandleData) -> Unit>>()
    
    fun subscribeToKlines(
        symbols: List<String>,
        interval: String,
        onUpdate: (String, CandleData) -> Unit
    ): Disposable {
        val streamNames = symbols.joinToString("/") { "${it.lowercase()}@kline_$interval" }
        val url = "wss://stream.binance.com:9443/stream?streams=$streamNames"
        
        val request = Request.Builder()
            .url(url)
            .build()
        
        val webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JsonParser.parseString(text).asJsonObject
                    val stream = json.get("stream").asString
                    val data = json.get("data").asJsonObject
                    val kline = data.get("k").asJsonObject
                    
                    val symbol = stream.substringBefore("@").uppercase()
                    val candle = CandleData(
                        open = kline.get("o").asString.toDouble(),
                        high = kline.get("h").asString.toDouble(),
                        low = kline.get("l").asString.toDouble(),
                        close = kline.get("c").asString.toDouble(),
                        volume = kline.get("v").asString.toDouble(),
                        time = kline.get("t").asLong
                    )
                    
                    onUpdate(symbol, candle)
                } catch (e: Exception) {
                    Log.e("WebSocketManager", "Error parsing message", e)
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketManager", "WebSocket failed", t)
            }
        })
        
        webSockets[url] = webSocket
        
        return Disposable {
            webSocket.close(1000, "Disposed by subscriber")
            webSockets.remove(url)
        }
    }
    
    fun closeAllConnections() {
        webSockets.values.forEach { it.close(1000, "App closing") }
        webSockets.clear()
    }
}

interface Disposable {
    fun dispose()
}