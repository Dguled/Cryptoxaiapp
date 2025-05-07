@Composable
fun AnimatedTabBar(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorOffset by animateDpAsState(
        targetValue = (selectedTab * 100).dp,
        animationSpec = tween(durationMillis = 300)
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colors.surface)
    ) {
        // Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth(1f / tabs.size)
                .height(2.dp)
                .offset(x = indicatorOffset)
                .align(Alignment.BottomStart)
                .background(MaterialTheme.colors.primary)
        )
        
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            tabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index) {
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        },
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}