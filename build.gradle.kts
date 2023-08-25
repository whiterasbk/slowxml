plugins {
    kotlin("multiplatform") version "1.9.0"
    application
    `maven-publish`
}

group = "com.github.whiterasbk"
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

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/whiterasbk/slowxml")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_CLASSIC_TOKEN")
            }
        }
    }

    publications {

        register<MavenPublication>("gpr") {
            from(components["kotlin"])
        }

        create<MavenPublication>("release") {
            groupId = "com.github.whiterasbk"
            artifactId = project.name
            version = project.version.toString()
            from(components["kotlin"])
        }
    }
}