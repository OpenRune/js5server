plugins {
    kotlin("jvm").version("1.9.23")
    application
    `maven-publish`
}

group = "dev.openrune"
version = "1.0.5"
description = "fast simple JS5 server"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.jire.js5server.Main")
    applicationDefaultJvmArgs += arrayOf(
        "-XX:-OmitStackTraceInFastThrow",

        "-Xmx8g",
        "-Xms4g",

        "-XX:+UseZGC",
        "-XX:MaxGCPauseMillis=100",

        "-Dio.netty.tryReflectionSetAccessible=true", // allow Netty to use direct buffer optimizations
        "-D data/cache 443,43594,50000 211 true"
    )
}

tasks.named("run", JavaExec::class) {
    args = listOf(
        "data/cache",
        "443,43594,50000",
        "211",
        "true"
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
    implementation("org.jctools:jctools-core:4.0.3")

    implementation("com.displee:rs-cache-library:7.1.3")
}

kotlin {
    jvmToolchain(17)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            url = uri("$buildDir/repo")
        }
        if (System.getenv("REPO_URL") != null) {
            maven {
                url = uri(System.getenv("REPO_URL"))
                credentials {
                    username = System.getenv("REPO_USERNAME")
                    password = System.getenv("REPO_PASSWORD")
                }
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}