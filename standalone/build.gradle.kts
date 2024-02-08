import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.22"
}

applyPlatformAndCoreConfiguration()
applyCommonArtifactoryConfig()
applyShadowConfiguration()

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
}

tasks.named<Jar>("jar") {
    val projectVersion = project.version
    inputs.property("projectVersion", projectVersion)
    manifest {
        attributes("Implementation-Version" to projectVersion)
        attributes("Main-Class" to "gg.netherite.votifier.standalone.VotifierStandalone")
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    dependencies {
        include(dependency(":nuvotifier-api"))
        include(dependency(":nuvotifier-common"))
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}