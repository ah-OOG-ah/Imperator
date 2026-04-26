import de.undercouch.gradle.tasks.download.Download

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

tasks.register<Download>("downloadMcVersionManifest") {
    src("https://launchermeta.mojang.com/mc/game/version_manifest.json")
    dest(cacheDir.file("all_versions_manifest.json"))
    overwrite(false)
    description = "Download the manifest JSON for MC jars."
}

tasks.run {
    dependsOn("downloadMcVersionManifest")
}

tasks.test {
    useJUnitPlatform()
}