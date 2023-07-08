plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:5.2.1.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-jetty")
    implementation("org.http4k:http4k-client-okhttp")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.http4k:http4k-format-kotlinx-serialization")

    implementation("ch.qos.logback:logback-classic:1.4.8")

    implementation(platform("dev.forkhandles:forkhandles-bom:2.6.0.0"))
    implementation("dev.forkhandles:result4k")

    testImplementation(kotlin("test"))
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("org.http4k:http4k-testing-hamkrest")
    testImplementation("org.http4k:http4k-testing-approval")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}