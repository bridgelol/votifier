import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
}

applyPlatformAndCoreConfiguration()

apply(plugin = "com.github.johnrengelman.shadow")
tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("dist")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}


repositories {
    mavenCentral()
    maven {
        name = "velocity"
        url = uri("https://nexus.velocitypowered.com/repository/maven-public/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    // Kotlinx Datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Project Dependencies
    implementation(project(":nuvotifier-api"))
    implementation(project(":nuvotifier-common"))

    // Redis
    implementation("redis.clients:jedis:5.2.0")

    // TOML Configuration
    implementation("com.akuleshov7:ktoml-core:0.5.1")
    implementation("com.akuleshov7:ktoml-file:0.5.1")
}

tasks.named<Jar>("jar") {
    val projectVersion = project.version
    inputs.property("projectVersion", projectVersion)
    manifest {
        attributes(
            "Implementation-Version" to projectVersion,
            "Main-Class" to "gg.netherite.votifier.standalone.VotifierStandalone"
        )
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}