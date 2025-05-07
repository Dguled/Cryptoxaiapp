@Composable
fun FibonacciOverlay(
    data: List<CandleData>,
    visibleRange: Pair<Float, Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val fibLevels = remember { listOf(0.0, 0.236, 0.382, 0.5, 0.618, 0.786, 1.0) }
    val (low, high) = remember(data) {
        val visibleData = data.subList(
            (visibleRange.first * data.size).toInt().coerceAtLeast(0),
            (visibleRange.second * data.size).toInt().coerceAtMost(data.size - 1)
        )
        visibleData.minOf { it.low } to visibleData.maxOf { it.high }
    }
    val range = high - low
    
    Canvas(modifier = modifier) {
        fibLevels.forEachIndexed { index, level ->
            val yPos = size.height - ((high - low * level) / range * size.height).toFloat()
            
            // Draw line
            drawLine(
                color = colors[index % colors.size],
                start = Offset(0f, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
            )
            
            // Draw label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "%.1f%%".format(level * 100),
                    10f,
                    yPos - 8.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = colors[index % colors.size].toArgb()
                        textSize = 12.sp.toPx()
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}