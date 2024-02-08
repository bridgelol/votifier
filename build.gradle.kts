logger.lifecycle("""
*******************************************
 You are building NuVotifier!
 If you encounter trouble:
 1) Try running 'build' in a separate Gradle run
 2) Use gradlew and not gradle
 3) If you still need help, you should reconsider building NuVotifier!

 Output files will be in [subproject]/build/libs
*******************************************
""")

allprojects {
    repositories {
        mavenCentral()
    }
}