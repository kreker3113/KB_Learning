package dev.kbwallet.app.profile.data

import dev.kbwallet.app.core.security.SecureTokenStorage
import dev.kbwallet.app.profile.domain.UserRepository
import dev.kbwallet.app.profile.presentation.ProfileState

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

class UserRepositoryImpl(
    private val secureStorage: SecureTokenStorage
) : UserRepository {

    private val KEY_DISPLAY_NAME = "user_display_name"
    private val KEY_EMAIL = "user_email"
    private val KEY_PUSH_NOTIFICATIONS = "user_push_notif"
    private val KEY_EMAIL_NOTIFICATIONS = "user_email_notif"
    private val KEY_PRICE_ALERTS = "user_price_alerts"
    private val KEY_TRADE_CONFIRM = "user_trade_confirm"
    private val KEY_NEWS_UPDATES = "user_news_updates"
    private val KEY_BIOMETRIC_AUTH = "user_biometric"
    private val KEY_TWO_FACTOR_AUTH = "user_two_factor"

    private val _profileStateFlow = MutableStateFlow<ProfileState?>(null)
    override val profileState: Flow<ProfileState> = _profileStateFlow.asStateFlow().filterNotNull()

    override suspend fun getProfileState(): ProfileState {
        val cached = _profileStateFlow.value
        if (cached != null) return cached

        val displayName = secureStorage.get(KEY_DISPLAY_NAME) ?: "Crypto Enthusiast"
        val email = secureStorage.get(KEY_EMAIL) ?: "crypto@example.com"
        val pushNotif = secureStorage.get(KEY_PUSH_NOTIFICATIONS)?.toBooleanStrictOrNull() ?: true
        val emailNotif = secureStorage.get(KEY_EMAIL_NOTIFICATIONS)?.toBooleanStrictOrNull() ?: false
        val priceAlerts = secureStorage.get(KEY_PRICE_ALERTS)?.toBooleanStrictOrNull() ?: true
        val tradeConfirm = secureStorage.get(KEY_TRADE_CONFIRM)?.toBooleanStrictOrNull() ?: true
        val newsUpdates = secureStorage.get(KEY_NEWS_UPDATES)?.toBooleanStrictOrNull() ?: false
        val biometric = secureStorage.get(KEY_BIOMETRIC_AUTH)?.toBooleanStrictOrNull() ?: false
        val twoFactor = secureStorage.get(KEY_TWO_FACTOR_AUTH)?.toBooleanStrictOrNull() ?: false

        val state = ProfileState(
            displayName = displayName,
            email = email,
            avatarInitial = displayName.firstOrNull()?.uppercase() ?: "C",
            pushNotifications = pushNotif,
            emailNotifications = emailNotif,
            priceAlerts = priceAlerts,
            tradeConfirmations = tradeConfirm,
            newsUpdates = newsUpdates,
            biometricAuth = biometric,
            twoFactorAuth = twoFactor
        )
        _profileStateFlow.value = state
        return state
    }

    override suspend fun saveProfileState(state: ProfileState) {
        _profileStateFlow.value = state
        secureStorage.save(KEY_DISPLAY_NAME, state.displayName)
        secureStorage.save(KEY_EMAIL, state.email)
        secureStorage.save(KEY_PUSH_NOTIFICATIONS, state.pushNotifications.toString())
        secureStorage.save(KEY_EMAIL_NOTIFICATIONS, state.emailNotifications.toString())
        secureStorage.save(KEY_PRICE_ALERTS, state.priceAlerts.toString())
        secureStorage.save(KEY_TRADE_CONFIRM, state.tradeConfirmations.toString())
        secureStorage.save(KEY_NEWS_UPDATES, state.newsUpdates.toString())
        secureStorage.save(KEY_BIOMETRIC_AUTH, state.biometricAuth.toString())
        secureStorage.save(KEY_TWO_FACTOR_AUTH, state.twoFactorAuth.toString())
    }
}
