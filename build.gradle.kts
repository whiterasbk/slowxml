plugins {
    kotlin("multiplatform") version "1.9.0"
    // id("ossrh.publication")
    // id("github.package")
    id("jitpack.release") apply false
    application
}

val releaseToMavenCentral = !true

group = (if (releaseToMavenCentral) "io" else "com") + ".github.whiterasbk" // "com.github.whiterasbk" for jitpack
version = "0.2.1"

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm {
        withJava()
        jvmToolchain(8)
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    js(IR) {
        browser {
            testTask {
                enabled = false
            }
        }
        nodejs()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

application {
    mainClass.set("io.whiterasbk.kotlin.slowxml.test.UnitTestKt")
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}

if (releaseToMavenCentral) {
    tasks.named("publishJvmPublicationToSonatypeRepository") {
        dependsOn(tasks.named("signJsPublication"))
        dependsOn(tasks.named("signKotlinMultiplatformPublication"))
    }

    tasks.named("publishJsPublicationToSonatypeRepository") {
        dependsOn(tasks.named("signJvmPublication"))
        dependsOn(tasks.named("signKotlinMultiplatformPublication"))
    }

    tasks.named("publishKotlinMultiplatformPublicationToSonatypeRepository") {
        dependsOn(tasks.named("signJsPublication"))
        dependsOn(tasks.named("signJvmPublication"))
    }
}