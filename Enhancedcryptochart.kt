@Composable
fun EnhancedCryptoChart(
    data: List<CandleData>,
    timeFrame: TimeFrame,
    indicators: ChartIndicators,
    showEMAs: Boolean = true,
    showVolume: Boolean = true,
    showFibonacci: Boolean = false,
    onZoom: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val chartColors = rememberChartColors()
    var visibleRange by remember { mutableStateOf(0f to 1f) }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                CombinedChart(context).apply {
                    setBackgroundColor(Color.Transparent.toArgb())
                    description.isEnabled = false
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setPinchZoom(true)
                    setDrawGridBackground(false)
                    legend.isEnabled = false
                    
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        valueFormatter = TimeAxisValueFormatter(timeFrame)
                        textColor = chartColors.textColor
                    }
                    
                    axisLeft.apply {
                        setDrawGridLines(true)
                        gridColor = chartColors.gridColor
                        textColor = chartColors.textColor
                        setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
                        setLabelCount(5, true)
                    }
                    
                    axisRight.isEnabled = false
                }
            },
            update = { chart ->
                // Update candle data
                val candleDataSet = CandleDataSet(data.map { it.toCandleEntry() }, "Price").apply {
                    setDrawIcons(false)
                    shadowColor = chartColors.shadowColor
                    shadowWidth = 1f
                    decreasingColor = chartColors.decreasingColor
                    decreasingPaintStyle = Paint.Style.FILL
                    increasingColor = chartColors.increasingColor
                    increasingPaintStyle = Paint.Style.FILL
                    neutralColor = chartColors.neutralColor
                    setDrawValues(false)
                }
                
                // Update EMA lines if enabled
                val emaDataSets = if (showEMAs) {
                    listOf(
                        indicators.ema20?.let { createEMALineDataSet(it, "EMA20", chartColors.ema20Color) },
                        indicators.ema50?.let { createEMALineDataSet(it, "EMA50", chartColors.ema50Color) },
                        indicators.ema100?.let { createEMALineDataSet(it, "EMA100", chartColors.ema100Color) },
                        indicators.ema200?.let { createEMALineDataSet(it, "EMA200", chartColors.ema200Color) }
                    ).filterNotNull()
                } else emptyList()
                
                // Update volume if enabled
                val barDataSet = if (showVolume) {
                    BarDataSet(data.mapIndexed { index, it -> 
                        BarEntry(index.toFloat(), it.volume.toFloat())
                    }, "Volume").apply {
                        color = chartColors.volumeColor
                        setDrawValues(false)
                    }
                } else null
                
                // Combine all data
                val combinedData = CombinedData().apply {
                    setData(createCandleData(candleDataSet))
                    if (emaDataSets.isNotEmpty()) {
                        setData(LineData(emaDataSets))
                    }
                    barDataSet?.let { setData(BarData(it)) }
                }
                
                chart.data = combinedData
                chart.invalidate()
                
                // Handle zoom and pan events
                chart.setOnChartGestureListener(object : OnChartGestureListener {
                    override fun onChartGestureStart(
                        me: MotionEvent?,
                        lastPerformedGesture: ChartTouchListener.ChartGesture?
                    ) {
                        visibleRange = chart.viewPortHandler.contentLeft() to 
                                    chart.viewPortHandler.contentRight()
                    }
                    
                    override fun onChartGestureEnd(
                        me: MotionEvent?,
                        lastPerformedGesture: ChartTouchListener.ChartGesture?
                    ) {
                        val newRange = chart.viewPortHandler.contentLeft() to 
                                      chart.viewPortHandler.contentRight()
                        if (newRange != visibleRange) {
                            visibleRange = newRange
                            onZoom(newRange.second - newRange.first)
                        }
                    }
                    
                    // Other required overrides...
                })
            }
        )
        
        // Fibonacci retracement overlay if enabled
        if (showFibonacci) {
            FibonacciOverlay(
                data = data,
                visibleRange = visibleRange,
                colors = chartColors.fibonacciColors,
                modifier = Modifier.matchParentSize()
            )
        }
        
        // Crosshair for precise price reading
        CrosshairOverlay(
            modifier = Modifier.matchParentSize(),
            textColor = chartColors.textColor
        )
    }
}

@Composable
private fun rememberChartColors(): ChartColors {
    val isDark = isSystemInDarkTheme()
    return remember(isDark) {
        if (isDark) {
            ChartColors(
                increasingColor = Color(0xFF4CAF50),
                decreasingColor = Color(0xFFF44336),
                neutralColor = Color(0xFF9E9E9E),
                shadowColor = Color(0x809E9E9E),
                gridColor = Color(0x309E9E9E),
                textColor = Color.White,
                volumeColor = Color(0x606673BC),
                ema20Color = Color(0xFFFF9800),
                ema50Color = Color(0xFF2196F3),
                ema100Color = Color(0xFFFFEB3B),
                ema200Color = Color(0xFF9C27B0),
                fibonacciColors = listOf(
                    Color(0xFFF44336), Color(0xFF2196F3), 
                    Color(0xFF4CAF50), Color(0xFFFFC107)
                )
            )
        } else {
            ChartColors(
                increasingColor = Color(0xFF2E7D32),
                decreasingColor = Color(0xFFC62828),
                neutralColor = Color(0xFF424242),
                shadowColor = Color(0x80424242),
                gridColor = Color(0x30424242),
                textColor = Color.Black,
                volumeColor = Color(0x606673BC),
                ema20Color = Color(0xFFEF6C00),
                ema50Color = Color(0xFF1565C0),
                ema100Color = Color(0xFFF9A825),
                ema200Color = Color(0xFF6A1B9A),
                fibonacciColors = listOf(
                    Color(0xFFD32F2F), Color(0xFF1976D2), 
                    Color(0xFF388E3C), Color(0xFFFFA000)
                )
           