rootProject.name = "nuvotifier"

include("nuvotifier-api")
project(":nuvotifier-api").projectDir = file("api")


include("nuvotifier-common")
project(":nuvotifier-common").projectDir = file("common")

include("nuvotifier-bukkit")
project(":nuvotifier-bukkit").projectDir = file("bukkit")
include("nuvotifier-bungeecord")
project(":nuvotifier-bungeecord").projectDir = file("bungeecord")
include("nuvotifier-velocity")
project(":nuvotifier-velocity").projectDir = file("velocity")

include("nuvotifier-universal")
project(":nuvotifier-universal").projectDir = file("universal")

include("nuvotifier-standalone")
project(":nuvotifier-standalone").projectDir = file("standalone")