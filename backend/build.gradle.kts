plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "org.pudink"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("org.jsoup:jsoup:1.18.1")

    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")

    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-cors")
    implementation("ch.qos.logback:logback-classic:1.4.8")

    implementation("com.apurebase:kgraphql:0.19.0")
    implementation("com.apurebase:kgraphql-ktor:0.19.0")

    implementation("com.expediagroup:graphql-kotlin-ktor-server:8.0.0")
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.10.1")

    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-cio-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-default-headers-jvm")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-okhttp")
    implementation("io.ktor:ktor-server-html-builder-jvm")
    implementation("io.ktor:ktor-server-swagger")


    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("io.mockk:mockk:1.13.11")

    testImplementation("io.ktor:ktor-client-mock")
    testImplementation("io.ktor:ktor-server-html-builder")
    testImplementation("io.ktor:ktor-server-test-host")

    implementation("org.jetbrains.exposed:exposed-core:0.54.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.54.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.54.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.54.0")

    implementation("com.h2database:h2:2.3.232")
    implementation("org.postgresql:postgresql:42.7.4")

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.2")

}

kotlin {
    jvmToolchain(21)
}

tasks.compileJava {
    options.release.set(21)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}
