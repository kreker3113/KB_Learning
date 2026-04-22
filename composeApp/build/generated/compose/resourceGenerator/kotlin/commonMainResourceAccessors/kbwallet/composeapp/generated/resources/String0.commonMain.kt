@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package kbwallet.composeapp.generated.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.StringResource

private object CommonMainString0 {
  public val error_disk_full: StringResource by 
      lazy { init_error_disk_full() }

  public val error_insufficient_balance: StringResource by 
      lazy { init_error_insufficient_balance() }

  public val error_no_internet: StringResource by 
      lazy { init_error_no_internet() }

  public val error_request_timeout: StringResource by 
      lazy { init_error_request_timeout() }

  public val error_serialization: StringResource by 
      lazy { init_error_serialization() }

  public val error_too_many_requests: StringResource by 
      lazy { init_error_too_many_requests() }

  public val error_unknown: StringResource by 
      lazy { init_error_unknown() }
}

@InternalResourceApi
internal fun _collectCommonMainString0Resources(map: MutableMap<String, StringResource>) {
  map.put("error_disk_full", CommonMainString0.error_disk_full)
  map.put("error_insufficient_balance", CommonMainString0.error_insufficient_balance)
  map.put("error_no_internet", CommonMainString0.error_no_internet)
  map.put("error_request_timeout", CommonMainString0.error_request_timeout)
  map.put("error_serialization", CommonMainString0.error_serialization)
  map.put("error_too_many_requests", CommonMainString0.error_too_many_requests)
  map.put("error_unknown", CommonMainString0.error_unknown)
}

internal val Res.string.error_disk_full: StringResource
  get() = CommonMainString0.error_disk_full

private fun init_error_disk_full(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:error_disk_full", "error_disk_full",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/kbwallet.composeapp.generated.resources/values/strings.commonMain.cvr", 10,
    75),
    )
)

internal val Res.string.error_insufficient_balance: StringResource
  get() = CommonMainString0.error_insufficient_balance

private fun init_error_insufficient_balance(): StringResource =
    org.jetbrains.compose.resources.StringResource(
  "string:error_insufficient_balance", "error_insufficient_balance",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/kbwallet.composeapp.generated.resources/values/strings.commonMain.cvr", 86,
    62),
    )
)

internal val Res.string.error_no_internet: StringResource
  get() = CommonMainString0.error_no_internet

private fun init_error_no_internet(): StringResource =
    org.jetbrains.compose.resources.StringResource(
  "string:error_no_internet", "error_no_internet",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/kbwallet.composeapp.generated.resources/values/strings.commonMain.cvr", 149,
    109),
    )
)

internal val Res.string.error_request_timeout: StringResource
  get() = CommonMainString0.error_request_timeout

private fun init_error_request_timeout(): StringResource =
    org.jetbrains.compose.resources.StringResource(
  "string:error_request_timeout", "error_request_timeout",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/kbwallet.composeapp.generated.resources/values/strings.commonMain.cvr", 259,
    61),
    )
)

internal val Res.string.error_serialization: StringResource
  get() = CommonMainString0.error_serialization

private fun init_error_serialization(): StringResource =
    org.jetbrains.compose.resources.StringResource(
  "string:error_serialization", "error_serialization",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/kbwallet.composeapp.generated.resources/values/strings.commonMain.cvr", 321,
    55),
    )
)

internal val Res.string.error_too_many_requests: StringResource
  get() = CommonMainString0.error_too_many_requests

private fun init_error_too_many_requests(): StringResource =
    org.jetbrains.compose.resources.StringResource(
  "string:error_too_many_requests", "error_too_many_requests",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/kbwallet.composeapp.generated.resources/values/strings.commonMain.cvr", 377,
    75),
    )
)

internal val Res.string.error_unknown: StringResource
  get() = CommonMainString0.error_unknown

private fun init_error_unknown(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:error_unknown", "error_unknown",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/kbwallet.composeapp.generated.resources/values/strings.commonMain.cvr", 453,
    57),
    )
)
