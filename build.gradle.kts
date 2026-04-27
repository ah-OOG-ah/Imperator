import de.undercouch.gradle.tasks.download.Download
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

plugins {
    id("java")
    id("application")
    id("de.undercouch.download") version "5.7.0"
}

group = "klaxon.klaxon.imperator"
version = "0.0.1"
val cacheDir = layout.buildDirectory.dir("iCache").get()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        version = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "$group.Main"
}

// Each MC version has a manifest, and this one lists *all* of them.
val dlMetaManifest = tasks.register<Download>("dlMetaManifest") {
    src("https://launchermeta.mojang.com/mc/game/version_manifest.json")
    dest(cacheDir.file("all_versions_manifest.json"))
    overwrite(false)
    description = "Download the manifest JSON for MC jars."
}

// And we grab the *real* manifest...
@OptIn(ExperimentalSerializationApi::class)
val dlManifest7p10 = tasks.register<Download>("dlManifest7.10") {
    dependsOn(dlMetaManifest)
    val metaMF = dlMetaManifest.get().outputFiles[0]
    val metaMFObj = Json.decodeFromStream<MetaMF>(metaMF.inputStream())
    val meta7p10 = metaMFObj.versions.find { it.id == "1.7.10" }

    src(meta7p10!!.url)
    dest(cacheDir.file("assets/indexes/1.7.10.json"))
    overwrite(false)
    description = "Download the manifest JSON for r7.10."
}

// Now that we have the manifest, we download EVERYTHING ELSE
@OptIn(ExperimentalSerializationApi::class)
val dlClient7p10 = tasks.register<Download>("dlClient7p10") {
    description = "Download everything needed to launch the game."

    dependsOn(dlManifest7p10)
    val versionMF = dlManifest7p10.get().outputFiles[0]
    val versionMFObj = Json.decodeFromStream<VersionMF>(versionMF.inputStream())

    src(versionMFObj.downloads.client.url)
    dest(cacheDir.file("libraries/client.jar"))
    overwrite(false)
}

tasks.run {
    dependsOn(dlClient7p10)
}

tasks.test {
    useJUnitPlatform()
}