package dev.kbwallet.app.profile.presentation

import androidx.lifecycle.ViewModel
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import dev.kbwallet.app.profile.domain.UserRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadSettings()
        loadStats()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.value = userRepository.getProfileState()
        }
    }

    private fun saveSettings() {
        viewModelScope.launch {
            userRepository.saveProfileState(_state.value)
        }
    }

    private fun loadStats() {
        // Stats would be loaded from repository
        // For now, using defaults from ProfileState
    }

    fun updateDisplayName(name: String) {
        _state.update {
            it.copy(
                displayName = name,
                avatarInitial = name.firstOrNull()?.uppercase() ?: "C"
            )
        }
        saveSettings()
    }

    fun updateEmail(email: String) {
        _state.update { it.copy(email = email) }
        saveSettings()
    }

    fun togglePushNotifications() {
        _state.update { it.copy(pushNotifications = !it.pushNotifications) }
        saveSettings()
    }

    fun toggleEmailNotifications() {
        _state.update { it.copy(emailNotifications = !it.emailNotifications) }
        saveSettings()
    }

    fun togglePriceAlerts() {
        _state.update { it.copy(priceAlerts = !it.priceAlerts) }
        saveSettings()
    }

    fun toggleTradeConfirmations() {
        _state.update { it.copy(tradeConfirmations = !it.tradeConfirmations) }
        saveSettings()
    }

    fun toggleNewsUpdates() {
        _state.update { it.copy(newsUpdates = !it.newsUpdates) }
        saveSettings()
    }

    fun toggleBiometricAuth() {
        _state.update { it.copy(biometricAuth = !it.biometricAuth) }
        saveSettings()
    }
    fun toggleTwoFactorAuth() {
        _state.update { it.copy(twoFactorAuth = !it.twoFactorAuth) }
        saveSettings()
    }
}
