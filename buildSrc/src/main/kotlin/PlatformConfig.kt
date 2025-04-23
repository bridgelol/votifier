import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named

fun Project.applyPlatformAndCoreConfiguration(javaRelease: Int = 17) {
    applyCommonConfiguration()
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    applyCommonJavaConfiguration(
        sourcesJar = true,
        javaRelease = javaRelease,
        banSlf4j = false
    )

    ext["internalVersion"] = "$version"
}

fun Project.applyShadowConfiguration() {
    apply(plugin = "com.github.johnrengelman.shadow")
    tasks.named<ShadowJar>("shadowJar") {
        archiveClassifier.set("dist")
        dependencies {
            include(project(":nuvotifier-api"))
            include(project(":nuvotifier-common"))
            include(dependency("redis.clients:jedis"))
        }

        exclude("com.google.code.findbugs:jsr305")
        exclude("GradleStart**")
        exclude(".cache")
        exclude("LICENSE*")
        exclude("META-INF/maven/**")
    }
    val javaComponent = components["java"] as AdhocComponentWithVariants
    // I don't think we want this published (it's the shadow jar)
    javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
        skip()
    }
}
