package dev.kbwallet.app.portfolio.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserBalanceDao {

    @Query("SELECT cashBalance FROM UserBalanceEntity WHERE id = 1")
    suspend fun getCashBalance(): Double?

    @Query("SELECT cashBalance FROM UserBalanceEntity WHERE id = 1")
    fun getCashBalanceFlow(): Flow<Double?>

    @Upsert
    suspend fun insertBalance(userBalanceEntity: UserBalanceEntity)

    @Query("UPDATE UserBalanceEntity SET cashBalance = :newBalance WHERE id = 1")
    suspend fun updateCashBalance(newBalance: Double)
}