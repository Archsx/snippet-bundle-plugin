plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.github.archsx"
version = "0.1.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.3")
    type.set("IC") // IC = Community, IU = Ultimate
    plugins.set(listOf(/* Add any needed plugins here */))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set("")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    runIde {
        // Add additional JVM options if needed
        jvmArgs = listOf("-Xmx2048m")
        // Use a custom sandbox directory to avoid configuration conflicts
        systemProperty("idea.config.path", file("${project.buildDir}/idea-config").absolutePath)
        systemProperty("idea.system.path", file("${project.buildDir}/idea-system").absolutePath)
        systemProperty("idea.log.path", file("${project.buildDir}/idea-log").absolutePath)
    }
}


