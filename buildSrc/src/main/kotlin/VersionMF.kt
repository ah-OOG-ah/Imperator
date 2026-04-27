import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.util.Optional

@Serializable
data class VersionMF(
    val assetIndex: AssetIndex,
    val assets: String,
    val complianceLevel: Int,
    val downloads: MainDownloads,
    val id: String,
    val javaVersion: JavaVersion,
    val libraries: List<Library>,
    val logging: Logging,
    val mainClass: String,
    val minecraftArguments: String,
    val minimumLauncherVersion: Int,
    val releaseTime: String,
    val time: String,
    val type: String
)

@Serializable
data class AssetIndex(
    val id: String,
    val sha1: String,
    val size: Int,
    val totalSize: Int,
    val url: String
)

@Serializable
data class MainDownloads(
    val client: MainDownload,
    val server: MainDownload,
    val windows_server: MainDownload
)

@Serializable
data class JavaVersion(
    val component: String,
    val majorVersion: Int
)

object LibraryDeser : JsonContentPolymorphicSerializer<Library>(Library::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Library> {
        val jsonObj = element.jsonObject
        return when {
            jsonObj.containsKey("extract") -> Library.WithNatives.serializer()
            else -> Library.Basic.serializer()
        }
    }
}

@Serializable(LibraryDeser::class)
sealed class Library {
    @Serializable
    data class Basic(
        val downloads: LibDownloads,
        val name: String,
    ): Library() {
        @Serializable
        data class LibDownloads(
            val artifact: Artifact
        )
    }

    @Serializable
    data class WithNatives(
        val downloads: NativeDownloads,
        val extract: Extract,
        val name: String,
        val natives: Natives,
        val rules: List<Rule> = listOf()
    ): Library() {
        @Serializable
        data class NativeDownloads(
            val classifiers: Map<String, Artifact>
        )
    }
}

@Serializable
data class Logging(
    val client: Client
)

@Serializable
data class MainDownload(
    val sha1: String,
    val size: Int,
    val url: String
)

@Serializable
data class Extract(
    val exclude: List<String>
)

@Serializable
data class Natives(
    val linux: String? = null,
    val osx: String? = null,
    val windows: String
)

@Serializable
data class Rule(
    val action: String,
    val os: Os? = null
)

@Serializable
data class Artifact(
    val path: String,
    val sha1: String,
    val size: Int,
    val url: String
)

@Serializable
data class Os(
    val name: String
)

@Serializable
data class Client(
    val argument: String,
    val `file`: File,
    val type: String
)

@Serializable
data class File(
    val id: String,
    val sha1: String,
    val size: Int,
    val url: String
)