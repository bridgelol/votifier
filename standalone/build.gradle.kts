import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

applyPlatformAndCoreConfiguration()
applyCommonArtifactoryConfig()

apply(plugin = "com.github.johnrengelman.shadow")
tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("dist")
}

repositories {
    maven {
        name = "velocity"
        url = uri("https://nexus.velocitypowered.com/repository/maven-public/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    implementation(project(":nuvotifier-api"))
    implementation(project(":nuvotifier-common"))

    implementation("redis.clients:jedis:5.1.0")

    implementation("com.akuleshov7:ktoml-core:0.5.1")
    implementation("com.akuleshov7:ktoml-file:0.5.1")
}

tasks.named<Jar>("jar") {
    val projectVersion = project.version
    inputs.property("projectVersion", projectVersion)
    manifest {
        attributes("Implementation-Version" to projectVersion)
        attributes("Main-Class" to "gg.netherite.votifier.standalone.VotifierStandalone")
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}