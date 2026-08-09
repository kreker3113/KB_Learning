package dev.kbwallet.app.profile.domain

import dev.kbwallet.app.profile.presentation.ProfileState

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val profileState: Flow<ProfileState>
    suspend fun getProfileState(): ProfileState
    suspend fun saveProfileState(state: ProfileState)
}
