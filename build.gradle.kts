plugins {
    java
    `maven-publish`
    kotlin("jvm") version "2.4.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "dev.elysium.eauth"

version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {url = uri("https://maven.mcbeeland.ru")}
}

dependencies {
    compileOnly(kotlin("stdlib"))

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.0")
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("dev.elysium.eapi:EAPI-paper:2.4")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
}

tasks {
    runServer {
        minecraftVersion("1.21.6")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
    }

    val sourcesJar by registering(Jar::class) {
        archiveBaseName.set("${rootProject.name}")
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = "${rootProject.name}"
            version = project.version.toString()

            artifact(tasks.named("sourcesJar"))
        }
    }
    repositories {
        maven {
            url = rootProject.layout.buildDirectory.dir("repo").get().asFile.toURI()
        }
    }
}
