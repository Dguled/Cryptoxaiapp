@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CryptoXAiTheme {
                Surface(color = MaterialTheme.colors.background) {
                    CryptoXAiApp()
                }
            }
        }
    }
}

@Composable
fun CryptoXAiApp() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") }
                )
                
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Screener") },
                    label = { Text("Screener") },
                    selected = currentRoute == "screener",
                    onClick = { navController.navigate("screener") }
                )
                
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Watchlist") },
                    label = { Text("Watchlist") },
                    selected = currentRoute == "watchlist",
                    onClick = { navController.navigate("watchlist") }
                )
                
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") { DashboardScreen() }
            composable("screener") { ScreenerScreen() }
            composable("watchlist") { WatchlistScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}