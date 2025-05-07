@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Theme selection
            SettingCategory(title = "Appearance")
            
            SettingItem(
                title = "Theme",
                subtitle = when (uiState.themePreference) {
                    ThemePreference.LIGHT -> "Light"
                    ThemePreference.DARK -> "Dark"
                    ThemePreference.SYSTEM -> "System default"
                },
                onClick = {
                    viewModel.showThemeDialog = true
                }
            )
            
            // Chart customization
            SettingCategory(title = "Chart Settings")
            
            SettingSwitchItem(
                title = "Show EMAs",
                checked = uiState.showEMAs,
                onCheckedChange = { viewModel.setShowEMAs(it) }
            )
            
            SettingSwitchItem(
                title = "Show Volume",
                checked = uiState.showVolume,
                onCheckedChange = { viewModel.setShowVolume(it) }
            )
            
            SettingSwitchItem(
                title = "Show Fibonacci",
                checked = uiState.showFibonacci,
                onCheckedChange = { viewModel.setShowFibonacci(it) }
            )
            
            // Notification settings
            SettingCategory(title = "Notifications")
            
            SettingSwitchItem(
                title = "Enable Alerts",
                checked = uiState.alertsEnabled,
                onCheckedChange = { viewModel.setAlertsEnabled(it) }
            )
            
            SettingItem(
                title = "Alert Sound",
                subtitle = uiState.alertSoundName,
                onClick = { viewModel.showSoundDialog = true }
            )
            
            // Strategy customization
            SettingCategory(title = "Strategy Parameters")
            
            SettingSliderItem(
                title = "Minimum Confidence",
                value = uiState.minConfidence,
                onValueChange = { viewModel.setMinConfidence(it) },
                valueRange = 50f..100f,
                steps = 5
            )
            
            // API settings
            SettingCategory(title = "API Configuration")
            
            SettingItem(
                title = "Binance API Key",
                subtitle = if (uiState.apiKey.isNotEmpty()) "••••••••${uiState.apiKey.takeLast(4)}" else "Not set",
                onClick = { viewModel.showApiKeyDialog = true }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Theme selection dialog
        if (viewModel.showThemeDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showThemeDialog = false },
                title = { Text("Select Theme") },
                text = {
                    Column {
                        ThemeOption(
                            title = "Light",
                            selected = uiState.themePreference == ThemePreference.LIGHT,
                            onClick = { viewModel.setThemePreference(ThemePreference.LIGHT) }
                        )
                        ThemeOption(
                            title = "Dark",
                            selected = uiState.themePreference == ThemePreference.DARK,
                            onClick = { viewModel.setThemePreference(ThemePreference.DARK) }
                        )
                        ThemeOption(
                            title = "System Default",
                            selected = uiState.themePreference == ThemePreference.SYSTEM,
                            onClick = { viewModel.setThemePreference(ThemePreference.SYSTEM) }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.showThemeDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Other dialogs...
    }
}

@Composable
private fun SettingCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.overline,
        color = MaterialTheme.colors.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.body1
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
    Divider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
}