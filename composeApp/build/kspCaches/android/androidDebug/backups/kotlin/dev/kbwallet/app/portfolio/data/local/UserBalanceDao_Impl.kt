package dev.kbwallet.app.portfolio.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserBalanceDao_Impl(
  __db: RoomDatabase,
) : UserBalanceDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfUserBalanceEntity: EntityUpsertAdapter<UserBalanceEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfUserBalanceEntity = EntityUpsertAdapter<UserBalanceEntity>(object :
        EntityInsertAdapter<UserBalanceEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `UserBalanceEntity` (`id`,`cashBalance`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserBalanceEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindDouble(2, entity.cashBalance)
      }
    }, object : EntityDeleteOrUpdateAdapter<UserBalanceEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `UserBalanceEntity` SET `id` = ?,`cashBalance` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: UserBalanceEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindDouble(2, entity.cashBalance)
        statement.bindLong(3, entity.id.toLong())
      }
    })
  }

  public override suspend fun insertBalance(userBalanceEntity: UserBalanceEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfUserBalanceEntity.upsert(_connection, userBalanceEntity)
  }

  public override suspend fun getCashBalance(): Double? {
    val _sql: String = "SELECT cashBalance FROM UserBalanceEntity WHERE id = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Double?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getDouble(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateCashBalance(newBalance: Double) {
    val _sql: String = "UPDATE UserBalanceEntity SET cashBalance = ? WHERE id = 1"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindDouble(_argIndex, newBalance)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
