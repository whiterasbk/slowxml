
import java.util.*


plugins {
    `maven-publish`
}

ext["gpr.user"] = null
ext["gpr.key"] = null

val secretPropsFile = project.rootProject.file("local.properties")

if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["gpr.user"] = System.getenv("GITHUB_USERNAME")
    ext["gpr.key"] = System.getenv("GITHUB_CLASSIC_TOKEN")
}


publishing {
    publications {
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

        register<MavenPublication>("gpr") {
            from(components["kotlin"])
        }
    }
}

