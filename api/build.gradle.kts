plugins {
    `java-library`
}

applyCommonJavaConfiguration(sourcesJar = true)

dependencies {
    "implementation"("com.google.code.gson:gson:${Versions.GSON}")
}
