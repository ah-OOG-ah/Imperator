@file:OptIn(ExperimentalSerializationApi::class)

import de.undercouch.gradle.tasks.download.Download
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.util.concurrent.CompletableFuture

plugins {
    id("java")
    id("application")
    id("de.undercouch.download") version "5.7.0"
}

group = "klaxon.klaxon.imperator"
version = "0.0.1"
val cacheDir = layout.buildDirectory.dir("iCache").get()
val metaMF = cacheDir.file("all_versions_manifest.json")
val mc7p10MF = cacheDir.file("assets/indexes/1.7.10.json")
val libraries = cacheDir.dir("libraries")
val mc7p10Jar = libraries.file("libraries/client.jar")

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
    dest(metaMF)
    overwrite(false)
    description = "Download the manifest JSON for MC jars."
}

// And we grab the *real* manifest...
val manifest7p10 = cacheDir.file("assets/indexes/1.7.10.json").asFile
val dlManifest7p10 = tasks.register("dlManifest7.10") {
    description = "Download the manifest JSON for r7.10."
    dependsOn(dlMetaManifest)
    doLast {
        val metaMFObj = Json.decodeFromStream<MetaMF>(dlMetaManifest.get().dest.inputStream())
        val meta7p10 = metaMFObj.versions.find { it.id == "1.7.10" }

        if (!manifest7p10.exists()) {
            download.run {
                src(meta7p10!!.url)
                dest(manifest7p10)
            }
        }
    }
}

// Now that we have the manifest, we download EVERYTHING ELSE
val dlClient7p10 = tasks.register("dlClient7p10") {
    description = "Download everything needed to launch the game."
    dependsOn(dlManifest7p10)

    doLast {
        val versionMFObj = Json.decodeFromStream<VersionMF>(manifest7p10.inputStream())

        val mainLoc = cacheDir.file("libraries/client.jar").asFile
        val mainDl = if (!mainLoc.exists()) {
            download.runAsync {
                src(versionMFObj.downloads.client.url)
                dest(mainLoc)
            }
        } else {
            CompletableFuture.allOf()
        }

        // And download the rest of the owl too.
        val libDLs = versionMFObj.libraries.mapNotNull {
            when {
                it is Library.Basic -> {
                    val location = cacheDir.file("libraries/" + it.downloads.artifact.path)
                    if (location.asFile.exists()) return@mapNotNull null

                    return@mapNotNull download.runAsync {
                        src(it.downloads.artifact.url)
                        dest(location)
                    }
                }
            }
            return@mapNotNull null
        }.toTypedArray()

        // Gotta await the futures.
        CompletableFuture.allOf(mainDl, *libDLs).get()
    }
}

tasks.run.configure {
    dependsOn(dlClient7p10)
    jvmArgs = jvmArgs + ("--enable-native-access=ALL-UNNAMED")

    doFirst {
        val versionMFObj = Json.decodeFromStream<VersionMF>(manifest7p10.inputStream())

        classpath += cacheDir.dir("libraries").asFileTree
        workingDir = projectDir.resolve("run/client")
        if (!workingDir.isDirectory) mkdir(workingDir)
        args(versionMFObj.mainClass, versionMFObj.minecraftArguments)
    }
}

tasks.test {
    useJUnitPlatform()
}