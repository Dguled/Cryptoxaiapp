@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoinCard(
    coin: CoinSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(if (isPressed) 8.dp else 2.dp)
    
    Surface(
        modifier = modifier
            .padding(8.dp)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = onClick,
                onLongClick = { /* Show details */ }
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = elevation,
        color = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = coin.iconUrl,
                    contentDescription = coin.name,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    error = painterResource(id = R.drawable.ic_crypto_placeholder)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = coin.symbol,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = coin.name,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = coin.priceFormatted,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${coin.change24h}%",
                        style = MaterialTheme.typography.body2,
                        color = if (coin.change24h >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Condition indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IndicatorChip(
                    label = "Trend",
                    active = coin.trendCondition,
                    activeColor = Color(0xFF2196F3)
                )
                IndicatorChip(
                    label = "Pullback",
                    active = coin.pullbackCondition,
                    activeColor = Color(0xFFFF9800)
                )
                IndicatorChip(
                    label = "Momentum",
                    active = coin.momentumCondition,
                    activeColor = Color(0xFF4CAF50)
                )
                IndicatorChip(
                    label = "Volume",
                    active = coin.volumeCondition,
                    activeColor = Color(0xFF9C27B0)
                )
            }
            
            // Confidence meter
            LinearProgressIndicator(
                progress = coin.confidence / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    coin.confidence >= 75 -> Color(0xFF4CAF50)
                    coin.confidence >= 50 -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                },
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun IndicatorChip(
    label: String,
    active: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) activeColor.copy(alpha = 0.2f) else MaterialTheme.colors.surface)
            .border(
                width = 1.dp,
                color = if (active) activeColor else MaterialTheme.colors.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = if (active) activeColor else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
}