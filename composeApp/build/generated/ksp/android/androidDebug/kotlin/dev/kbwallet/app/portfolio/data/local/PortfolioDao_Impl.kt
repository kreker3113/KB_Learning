package dev.kbwallet.app.portfolio.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PortfolioDao_Impl(
  __db: RoomDatabase,
) : PortfolioDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfPortfolioCoinEntity: EntityUpsertAdapter<PortfolioCoinEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfPortfolioCoinEntity = EntityUpsertAdapter<PortfolioCoinEntity>(object :
        EntityInsertAdapter<PortfolioCoinEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `PortfolioCoinEntity` (`coinId`,`name`,`symbol`,`iconUrl`,`averagePurchasePrice`,`amountOwned`,`timestamp`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PortfolioCoinEntity) {
        statement.bindText(1, entity.coinId)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.symbol)
        statement.bindText(4, entity.iconUrl)
        statement.bindDouble(5, entity.averagePurchasePrice)
        statement.bindDouble(6, entity.amountOwned)
        statement.bindLong(7, entity.timestamp)
      }
    }, object : EntityDeleteOrUpdateAdapter<PortfolioCoinEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `PortfolioCoinEntity` SET `coinId` = ?,`name` = ?,`symbol` = ?,`iconUrl` = ?,`averagePurchasePrice` = ?,`amountOwned` = ?,`timestamp` = ? WHERE `coinId` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PortfolioCoinEntity) {
        statement.bindText(1, entity.coinId)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.symbol)
        statement.bindText(4, entity.iconUrl)
        statement.bindDouble(5, entity.averagePurchasePrice)
        statement.bindDouble(6, entity.amountOwned)
        statement.bindLong(7, entity.timestamp)
        statement.bindText(8, entity.coinId)
      }
    })
  }

  public override suspend fun insert(portfolioCoinEntity: PortfolioCoinEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfPortfolioCoinEntity.upsert(_connection, portfolioCoinEntity)
  }

  public override fun getAllOwnedCoins(): Flow<List<PortfolioCoinEntity>> {
    val _sql: String = "SELECT * FROM PortfolioCoinEntity ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("PortfolioCoinEntity")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfCoinId: Int = getColumnIndexOrThrow(_stmt, "coinId")
        val _cursorIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _cursorIndexOfSymbol: Int = getColumnIndexOrThrow(_stmt, "symbol")
        val _cursorIndexOfIconUrl: Int = getColumnIndexOrThrow(_stmt, "iconUrl")
        val _cursorIndexOfAveragePurchasePrice: Int = getColumnIndexOrThrow(_stmt,
            "averagePurchasePrice")
        val _cursorIndexOfAmountOwned: Int = getColumnIndexOrThrow(_stmt, "amountOwned")
        val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<PortfolioCoinEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PortfolioCoinEntity
          val _tmpCoinId: String
          _tmpCoinId = _stmt.getText(_cursorIndexOfCoinId)
          val _tmpName: String
          _tmpName = _stmt.getText(_cursorIndexOfName)
          val _tmpSymbol: String
          _tmpSymbol = _stmt.getText(_cursorIndexOfSymbol)
          val _tmpIconUrl: String
          _tmpIconUrl = _stmt.getText(_cursorIndexOfIconUrl)
          val _tmpAveragePurchasePrice: Double
          _tmpAveragePurchasePrice = _stmt.getDouble(_cursorIndexOfAveragePurchasePrice)
          val _tmpAmountOwned: Double
          _tmpAmountOwned = _stmt.getDouble(_cursorIndexOfAmountOwned)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_cursorIndexOfTimestamp)
          _item =
              PortfolioCoinEntity(_tmpCoinId,_tmpName,_tmpSymbol,_tmpIconUrl,_tmpAveragePurchasePrice,_tmpAmountOwned,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCoinById(coinId: String): PortfolioCoinEntity? {
    val _sql: String = "SELECT * FROM PortfolioCoinEntity WHERE coinId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, coinId)
        val _cursorIndexOfCoinId: Int = getColumnIndexOrThrow(_stmt, "coinId")
        val _cursorIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _cursorIndexOfSymbol: Int = getColumnIndexOrThrow(_stmt, "symbol")
        val _cursorIndexOfIconUrl: Int = getColumnIndexOrThrow(_stmt, "iconUrl")
        val _cursorIndexOfAveragePurchasePrice: Int = getColumnIndexOrThrow(_stmt,
            "averagePurchasePrice")
        val _cursorIndexOfAmountOwned: Int = getColumnIndexOrThrow(_stmt, "amountOwned")
        val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: PortfolioCoinEntity?
        if (_stmt.step()) {
          val _tmpCoinId: String
          _tmpCoinId = _stmt.getText(_cursorIndexOfCoinId)
          val _tmpName: String
          _tmpName = _stmt.getText(_cursorIndexOfName)
          val _tmpSymbol: String
          _tmpSymbol = _stmt.getText(_cursorIndexOfSymbol)
          val _tmpIconUrl: String
          _tmpIconUrl = _stmt.getText(_cursorIndexOfIconUrl)
          val _tmpAveragePurchasePrice: Double
          _tmpAveragePurchasePrice = _stmt.getDouble(_cursorIndexOfAveragePurchasePrice)
          val _tmpAmountOwned: Double
          _tmpAmountOwned = _stmt.getDouble(_cursorIndexOfAmountOwned)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_cursorIndexOfTimestamp)
          _result =
              PortfolioCoinEntity(_tmpCoinId,_tmpName,_tmpSymbol,_tmpIconUrl,_tmpAveragePurchasePrice,_tmpAmountOwned,_tmpTimestamp)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deletePortfolioItem(coinId: String) {
    val _sql: String = "DELETE FROM PortfolioCoinEntity WHERE coinId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, coinId)
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
