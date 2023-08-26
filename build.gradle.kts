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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.jsoup:jsoup:1.16.1")

    implementation("io.ktor:ktor-server-html-builder-jvm:2.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")

    implementation("io.ktor:ktor-server-swagger:2.3.3")
    implementation("io.ktor:ktor-server-cors:2.3.3")
    implementation("ch.qos.logback:logback-classic:1.4.8")

    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.10.1")

    implementation("io.ktor:ktor-server-core-jvm:2.3.3")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.3")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.3")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.3.3")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:2.3.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.3")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("io.mockk:mockk:1.13.5")

    testImplementation("io.ktor:ktor-client-mock:2.3.3")
    testImplementation("io.ktor:ktor-server-html-builder:2.3.3")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}