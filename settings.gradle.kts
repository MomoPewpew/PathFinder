plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.4.0")
}

rootProject.name = "pathfinder"

sequenceOf(
        "api",
    "citizens",
        "core",
        "bukkit",
        "graph",
        "editmode",
        "papi",
        "quests-module",
        "scripted-visualizer",
).forEach {
    val name = "${rootProject.name}-$it"
    include(name)
    project(":$name").projectDir = file(name)
}
include("pathfinder-test-utils")
include("vendor-disposables-api")
include("vendor-disposables-bukkit")
include("vendor-legacy-libs")
project(":vendor-disposables-api").projectDir = file("vendor/disposables-api")
project(":vendor-disposables-bukkit").projectDir = file("vendor/disposables-bukkit")
project(":vendor-legacy-libs").projectDir = file("vendor/legacy-libs")
