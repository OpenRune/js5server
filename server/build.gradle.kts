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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            url = uri("$buildDir/repo")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}