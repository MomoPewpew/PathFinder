plugins {
    id("io.freefair.lombok") version "8.14.4"
    id("com.gradleup.shadow") version "9.4.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

val paperApiVersion = project.property("paper_api_version") as String

repositories {
    mavenCentral()
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(project(":pathfinder-bukkit"))
    testImplementation(project(":pathfinder-bukkit"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    compileOnly("com.mojang:authlib:1.5.25")
    testImplementation("io.papermc.paper:paper-api:$paperApiVersion")

    implementation("de.tr7zw:item-nbt-api:2.16.0")
    compileOnly(project(mapOf("path" to ":vendor-legacy-libs", "configuration" to "menuframeworkLibs")))
    compileOnly(project(mapOf("path" to ":vendor-legacy-libs", "configuration" to "translationsLibs")))
    implementation("xyz.xenondevs:particle:1.8.4")
    implementation(files("../libs/ClientEntities-1.3.6.jar"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    processResources {
        filter(
            org.apache.tools.ant.filters.ReplaceTokens::class,
            "tokens" to mapOf(
                "version" to project.version.toString(),
                "name" to rootProject.name
            )
        )
    }
    shadowJar {
        dependencies {
            include(project(mapOf("path" to ":vendor-legacy-libs", "configuration" to "menuframeworkLibs")))
            include(dependency("de.cubbossa:ClientEntities:.*"))
            include(dependency("xyz.xenondevs:particle:.*"))
            include(dependency("de.tr7zw:item-nbt-api:.*"))
            include(dependency("de.item-nbt-api:.*"))
        }

        fun relocateLib(from: String, to: String) {
            relocate(from, "de.cubbossa.pathfinder.lib.$to")
        }

        relocateLib("de.cubbossa.menuframework", "gui")
        relocateLib("de.cubbossa.cliententities", "cliententities")
        relocateLib("xyz.xenondevs.particle", "particle")
        relocateLib("de.tr7zw.changeme.nbtapi", "nbtapi")
    }
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
