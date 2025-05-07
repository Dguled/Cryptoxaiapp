@Composable
fun CryptoChart(
    candles: List<CandleData>,
    modifier: Modifier = Modifier,
    showVolume: Boolean = true
) {
    AndroidView(
        factory = { context ->
            CombinedChart(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
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
                    valueFormatter = TimestampAxisFormatter()
                    textColor = Color.GRAY
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    textColor = Color.GRAY
                    setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
                    setLabelCount(5, true)
                }
                
                axisRight.isEnabled = false
            }
        },
        modifier = modifier,
        update = { chart ->
            // Prepare candle data
            val candleEntries = candles.mapIndexed { index, candle ->
                CandleEntry(
                    index.toFloat(),
                    candle.high.toFloat(),
                    candle.low.toFloat(),
                    candle.open.toFloat(),
                    candle.close.toFloat()
                )
            }
            
            val candleDataSet = CandleDataSet(candleEntries, "Price").apply {
                setDrawIcons(false)
                shadowColor = Color.DKGRAY
                shadowWidth = 1f
                decreasingColor = Color.RED
                decreasingPaintStyle = Paint.Style.FILL
                increasingColor = Color.GREEN
                increasingPaintStyle = Paint.Style.FILL
                neutralColor = Color.GRAY
                setDrawValues(false)
            }
            
            // Prepare volume data if needed
            val barDataSet = if (showVolume) {
                BarDataSet(
                    candles.mapIndexed { index, candle ->
                        BarEntry(index.toFloat(), candle.volume.toFloat())
                    },
                    "Volume"
                ).apply {
                    color = Color.BLUE
                    setDrawValues(false)
                }
            } else null
            
            // Combine data
            val combinedData = CombinedData().apply {
                setData(CandleData(candleDataSet))
                if (barDataSet != null) {
                    setData(BarData(barDataSet))
                }
            }
            
            chart.data = combinedData
            chart.invalidate()
        }
    )
}

class TimestampAxisFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(value.toLong() * 1000))
    }
}