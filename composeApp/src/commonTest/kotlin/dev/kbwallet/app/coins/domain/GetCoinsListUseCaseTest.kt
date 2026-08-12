package dev.kbwallet.app.coins.domain

import dev.kbwallet.app.coins.data.remote.FakeCoinsRemoteDataSource
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCoinsListUseCaseTest {

    private lateinit var useCase: GetCoinsListUseCase
    private lateinit var dataSource: FakeCoinsRemoteDataSource

    @BeforeTest
    fun setup() {
        dataSource = FakeCoinsRemoteDataSource()
        useCase = GetCoinsListUseCase(dataSource)
    }

    @Test
    fun `Successful fetch maps DTO to domain model`() = runTest {
        val result = useCase.execute()
        assertTrue(result is Result.Success)
        
        val list = (result as Result.Success).data
        assertEquals(1, list.size)
        assertEquals("1", list[0].coin.id)
        assertEquals("Bitcoin", list[0].coin.name)
        assertEquals("BTC", list[0].coin.symbol)
        assertEquals(50000.0, list[0].price)
    }

    @Test
    fun `Error returns corresponding result`() = runTest {
        dataSource.simulateError = true
        val result = useCase.execute()
        assertTrue(result is Result.Error)
        assertEquals(DataError.Remote.SERVER, (result as Result.Error).error)
    }
}
