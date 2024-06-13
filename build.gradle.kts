plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow")

    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

group = "org.jire"
version = "1.0.4"
description = "fast simple JS5 server"

application {
    mainClass.set("org.jire.js5server.Main")
    applicationDefaultJvmArgs += arrayOf(
        "-XX:-OmitStackTraceInFastThrow",

        "-Xmx8g",
        "-Xms4g",

        "-XX:+UseZGC",
        "-XX:MaxGCPauseMillis=100",

        "-Dio.netty.tryReflectionSetAccessible=true", // allow Netty to use direct buffer optimizations
    )
}

dependencies {
    val slf4jVersion = "2.0.12"
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    runtimeOnly("org.slf4j:slf4j-simple:$slf4jVersion")

    for (module in listOf(
        "handler",
        "buffer",
        "transport-native-epoll",
        "transport-native-kqueue",
    )) implementation("io.netty:netty-$module:4.1.107.Final")

    implementation("io.netty.incubator:netty-incubator-transport-native-io_uring:0.0.25.Final")

    implementation("it.unimi.dsi:fastutil:8.5.13")

    implementation("com.displee:rs-cache-library:7.1.3")
}
