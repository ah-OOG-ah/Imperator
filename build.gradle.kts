plugins {
    id("java")
    id("application")
}

group = "klaxon.klaxon.imperator"
version = "0.0.1"

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

tasks.test {
    useJUnitPlatform()
}