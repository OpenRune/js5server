plugins {
    kotlin("jvm") version "1.9.23"
    id("maven-publish")
}

group = "dev.openrune"
version = "2.1"
val buildDirectory = "E:\\RSPS\\OpenRune\\hosting"

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/OpenRune/hosting/master")
}

dependencies {
    implementation(rootProject.libs.openrune.filesystem)
    implementation(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(rootProject.libs.netty.handler)
    implementation(rootProject.libs.netty.native.epoll)
    implementation(rootProject.libs.netty.native.kqueue)
    implementation(rootProject.libs.netty.incubator.iouring)
    implementation(rootProject.libs.netty.native.macos.dns.resolver)
    val epollClassifiers = listOf("linux-aarch_64", "linux-x86_64", "linux-riscv64")
    val kqueueClassifiers = listOf("osx-x86_64")
    val iouringClassifiers = listOf("linux-aarch_64", "linux-x86_64")
    for (classifier in epollClassifiers) {
        implementation(variantOf(rootProject.libs.netty.native.epoll) { classifier(classifier) })
    }
    for (classifier in kqueueClassifiers) {
        implementation(variantOf(rootProject.libs.netty.native.kqueue) { classifier(classifier) })
    }
    for (classifier in iouringClassifiers) {
        implementation(variantOf(rootProject.libs.netty.incubator.iouring) { classifier(classifier) })
    }
    implementation(rootProject.libs.inline.logger)
    api(rootProject.libs.rsprot.protocol)
    api(rootProject.libs.rsprot.compression)
    api(rootProject.libs.rsprot.crypto)
    api(rootProject.libs.rsprot.osrs229.common)
    api(rootProject.libs.rsprot.osrs229.model)
    implementation(rootProject.libs.rsprot.osrs229.internal)
    implementation(rootProject.libs.rsprot.osrs229.desktop)
    implementation(rootProject.libs.rsprot.osrs229.shared)
    implementation(rootProject.libs.rsprot.buffer)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifactId = "js5server"

            pom {
                name.set("OpenRune - ${project.name}")
                description.set("Module ${project.name} of the OpenRune project.")
                url.set("https://github.com/OpenRune")

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("openrune")
                        name.set("OpenRune Team")
                        email.set("contact@openrune.dev")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/OpenRune.git")
                    developerConnection.set("scm:git:ssh://github.com/OpenRune.git")
                    url.set("https://github.com/OpenRune")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(buildDirectory)
        }
    }
}


plugins.withType<MavenPublishPlugin> {
    configure<PublishingExtension> {
        publications.withType<MavenPublication> {
            groupId = "dev.openrune"
            artifactId = "js5server"
            version = version
        }
    }
}