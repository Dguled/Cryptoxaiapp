@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp
) {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colors.surface.copy(alpha = 0.3f),
            MaterialTheme.colors.surface.copy(alpha = 0.5f),
            MaterialTheme.colors.surface.copy(alpha = 0.3f)
        ),
        start = Offset(translateAnim - 500, translateAnim - 500),
        end = Offset(translateAnim, translateAnim)
    )
    
    Box(
        modifier = modifier
            .background(brush = brush)
            .clip(RoundedCornerShape(cornerRadius))
    )
}