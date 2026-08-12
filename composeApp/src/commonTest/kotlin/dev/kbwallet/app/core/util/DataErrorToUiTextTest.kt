package dev.kbwallet.app.core.util

import dev.kbwallet.app.core.domain.DataError
import kblearning.composeapp.generated.resources.Res
import kblearning.composeapp.generated.resources.error_disk_full
import kblearning.composeapp.generated.resources.error_insufficient_balance
import kblearning.composeapp.generated.resources.error_no_internet
import kblearning.composeapp.generated.resources.error_request_timeout
import kblearning.composeapp.generated.resources.error_serialization
import kblearning.composeapp.generated.resources.error_too_many_requests
import kblearning.composeapp.generated.resources.error_unknown
import kotlin.test.Test
import kotlin.test.assertEquals

class DataErrorToUiTextTest {

    @Test
    fun `Remote REQUEST_TIMEOUT maps to request timeout string`() {
        assertEquals(Res.string.error_request_timeout, DataError.Remote.REQUEST_TIMEOUT.toUiText())
    }

    @Test
    fun `Remote TOO_MANY_REQUESTS maps to too many requests string`() {
        assertEquals(Res.string.error_too_many_requests, DataError.Remote.TOO_MANY_REQUESTS.toUiText())
    }

    @Test
    fun `Remote NO_INTERNET maps to no internet string`() {
        assertEquals(Res.string.error_no_internet, DataError.Remote.NO_INTERNET.toUiText())
    }

    @Test
    fun `Remote SERVER maps to unknown error string`() {
        assertEquals(Res.string.error_unknown, DataError.Remote.SERVER.toUiText())
    }

    @Test
    fun `Remote SERIALIZATION maps to serialization error string`() {
        assertEquals(Res.string.error_serialization, DataError.Remote.SERIALIZATION.toUiText())
    }

    @Test
    fun `Remote UNKNOWN maps to unknown error string`() {
        assertEquals(Res.string.error_unknown, DataError.Remote.UNKNOWN.toUiText())
    }

    @Test
    fun `Local DISK_FULL maps to disk full string`() {
        assertEquals(Res.string.error_disk_full, DataError.Local.DISK_FULL.toUiText())
    }

    @Test
    fun `Local INSUFFICIENT_FUNDS maps to insufficient balance string`() {
        assertEquals(Res.string.error_insufficient_balance, DataError.Local.INSUFFICIENT_FUNDS.toUiText())
    }

    @Test
    fun `Local UNKNOWN maps to unknown error string`() {
        assertEquals(Res.string.error_unknown, DataError.Local.UNKNOWN.toUiText())
    }
}
