import kotlinx.serialization.Serializable

@Serializable
data class Latest(val release: String, val snapshot: String)
@Serializable
data class VersionMeta(val id: String, val type: String, val url: String, val time: String, val releaseTime: String)
@Suppress("ArrayInDataClass") // I'm not using equals or hashcode on this
@Serializable
data class MetaMF(val latest: Latest, val versions: Array<VersionMeta>)