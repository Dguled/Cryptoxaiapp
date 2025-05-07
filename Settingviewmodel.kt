@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    var showThemeDialog by mutableStateOf(false)
        private set
    var showSoundDialog by mutableStateOf(false)
        private set
    var showApiKeyDialog by mutableStateOf(false)
        private set
    
    init {
        viewModelScope.launch {
            combine(
                preferences.themePreferenceFlow,
                preferences.showEMAsFlow,
                preferences.showVolumeFlow,
                preferences.showFibonacciFlow,
                preferences.alertsEnabledFlow,
                preferences.alertSoundFlow,
                preferences.minConfidenceFlow,
                preferences.apiKeyFlow
            ) { theme, emas, volume, fib, alerts, sound, confidence, apiKey ->
                SettingsUiState(
                    themePreference = theme,
                    showEMAs = emas,
                    showVolume = volume,
                    showFibonacci = fib,
                    alertsEnabled = alerts,
                    alertSoundName = sound?.name ?: "Default",
                    minConfidence = confidence,
                    apiKey = apiKey ?: ""
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    fun setThemePreference(preference: ThemePreference) {
        viewModelScope.launch {
            preferences.setThemePreference(preference)
        }
    }
    
    fun setShowEMAs(show: Boolean) {
        viewModelScope.launch {
            preferences.setShowEMAs(show)
        }
    }
    
    // Similar functions for other preferences...
    
    data class SettingsUiState(
        val themePreference: ThemePreference = ThemePreference.SYSTEM,
        val showEMAs: Boolean = true,
        val showVolume: Boolean = true,
        val showFibonacci: Boolean = false,
        val alertsEnabled: Boolean = true,
        val alertSoundName: String = "Default",
        val minConfidence: Float = 70f,
        val apiKey: String = ""
    )
}