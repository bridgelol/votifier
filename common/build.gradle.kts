plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()

dependencies {
    "api"(project(":nuvotifier-api"))
    "api"("redis.clients:jedis:5.2.0")
    "implementation"("io.netty:netty-handler:${Versions.NETTYIO}")
    "implementation"("io.netty:netty-transport-native-epoll:${Versions.NETTYIO}:linux-x86_64")
    "implementation"("com.google.code.gson:gson:${Versions.GSON}")
    "testImplementation"("org.json:json:20180130") // retain this for testing reasons
    "testImplementation"("com.google.guava:guava:28.1-jre")
}