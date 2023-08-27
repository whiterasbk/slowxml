plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("Speed_testKt")
}

dependencies {
    implementation(rootProject)
    implementation("org.dom4j:dom4j:2.1.4")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
}