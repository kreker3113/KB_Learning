package dev.kbwallet.app.profile.domain

import dev.kbwallet.app.profile.presentation.ProfileState

interface UserRepository {
    suspend fun getProfileState(): ProfileState
    suspend fun saveProfileState(state: ProfileState)
}
