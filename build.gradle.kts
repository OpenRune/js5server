plugins {
    kotlin("jvm") version "2.0.21"
}

group = "dev.openrune"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://raw.githubusercontent.com/OpenRune/hosting/master")
}

dependencies {
    implementation(rootProject.libs.openrune.filesystem)
    implementation(rootProject.libs.openrune.displee)

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