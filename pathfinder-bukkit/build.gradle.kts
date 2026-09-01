import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.4.1"
    id("io.freefair.lombok") version "8.14.4"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
}

group = "de.cubbossa"

val minecraftVersion = project.property("minecraft_version") as String
val paperApiVersion = project.property("paper_api_version") as String
val commandApiVersion = project.property("commandapi_version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://repo.codemc.org/repository/maven-snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven {
        url = uri("https://nexus.leonardbausenwein.de/repository/maven-public/")
        content {
            includeGroupByRegex("de\\.cubbossa")
        }
    }
}

dependencies {

    api(project(":pathfinder-api"))
    api(project(":pathfinder-core"))
    api(project(":vendor-disposables-bukkit"))
    runtimeOnly(project(":pathfinder-scripted-visualizer"))

    compileOnlyApi("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly(project(mapOf("path" to ":vendor-legacy-libs", "configuration" to "translationsLibs")))
    compileOnly(project(mapOf("path" to ":vendor-legacy-libs", "configuration" to "splinelibLibs")))
    testImplementation(project(mapOf("path" to ":vendor-legacy-libs", "configuration" to "translationsLibs")))
    testImplementation(project(mapOf("path" to ":vendor-legacy-libs", "configuration" to "splinelibLibs")))

    testImplementation("io.papermc.paper:paper-api:$paperApiVersion")

    api("dev.jorel:commandapi-paper-shade:$commandApiVersion")

    implementation("org.bstats:bstats-bukkit:3.0.1")

    testImplementation(project(":pathfinder-test-utils"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(files("generated/plugin-yml/Bukkit/plugin.yml"))
}

configurations {
    create("legacyLibs") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }
}

dependencies {
    add("legacyLibs", project(mapOf("path" to ":vendor-legacy-libs", "configuration" to "splinelibLibs")))
    add("legacyLibs", project(mapOf("path" to ":vendor-legacy-libs", "configuration" to "translationsLibs")))
}

sourceSets {
    main {
        java.srcDirs += file("build/generated-src/antlr/main")
        resources {
            exclude("*.db")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

bukkit {
    name = "PathFinder"
    version = rootProject.version.toString()
    description = null
    website = "https://docs.leonardbausenwein.de"
    author = "CubBossa"

    main = "de.cubbossa.pathfinder.PathFinderPlugin"

    apiVersion = "26.2"

    softDepend = listOf(
            "PlaceholderAPI",
            "ProtocolLib",
            "ProtocolSupport",
            "ViaVersion",
            "ViaBackwards",
            "ViaRewind",
            "Geyser-Spigot"
    )
    libraries = listOf(
            "org.openjdk.nashorn:nashorn-core:15.4",
            "org.snakeyaml:snakeyaml-engine:2.0",
            "com.zaxxer:HikariCP:5.0.1",
            "org.antlr:antlr4-runtime:4.12.0",
            "org.jooq:jooq:3.16.23",
            "com.github.ben-manes.caffeine:caffeine:3.1.6",
            "io.github.toxicity188:adventure-platform-bukkit:5.2.0"
    )

    defaultPermission = BukkitPluginDescription.Permission.Default.OP
    permissions {
        register("pathfinder.command.pathfinder.info")
        register("pathfinder.command.pathfinder.help")
        register("pathfinder.command.pathfinder.reload")
        register("pathfinder.command.pathfinder.import")
        register("pathfinder.command.pathfinder.export")
        register("pathfinder.command.discoveries") { default = BukkitPluginDescription.Permission.Default.TRUE }
        register("pathfinder.command.find") { default = BukkitPluginDescription.Permission.Default.TRUE }
        register("pathfinder.command.findlocation")
        register("pathfinder.command.findplayer.request")
        register("pathfinder.command.findplayer.accept")
        register("pathfinder.command.findplayer.decline")
        register("pathfinder.command.cancel_path") { default = BukkitPluginDescription.Permission.Default.TRUE }
        register("pathfinder.command.roadmap.info")
        register("pathfinder.command.roadmap.create")
        register("pathfinder.command.roadmap.delete")
        register("pathfinder.command.roadmap.editmode")
        register("pathfinder.command.roadmap.list")
        register("pathfinder.command.roadmap.forcefind")
        register("pathfinder.command.roadmap.forceforget")
        register("pathfinder.command.roadmap.set_visualizer")
        register("pathfinder.command.roadmap.set_name")
        register("pathfinder.command.roadmap.set_curvelength")
        register("pathfinder.command.nodegroup.list")
        register("pathfinder.command.nodegroup.create")
        register("pathfinder.command.nodegroup.delete")
        register("pathfinder.command.nodegroup.set_name")
        register("pathfinder.command.nodegroup.set_findable")
        register("pathfinder.command.nodegroup.searchterms.list")
        register("pathfinder.command.nodegroup.searchterms.add")
        register("pathfinder.command.nodegroup.searchterms.remove")
        register("pathfinder.command.waypoint.info")
        register("pathfinder.command.waypoint.list")
        register("pathfinder.command.waypoint.create")
        register("pathfinder.command.waypoint.delete")
        register("pathfinder.command.waypoint.tp")
        register("pathfinder.command.waypoint.tphere")
        register("pathfinder.command.waypoint.connect")
        register("pathfinder.command.waypoint.disconnect")
        register("pathfinder.command.waypoint.set_curve_length")
        register("pathfinder.command.waypoint.add_group")
        register("pathfinder.command.waypoint.remove_group")
        register("pathfinder.command.waypoint.clear_groups")
        register("pathfinder.command.visualizer.list")
        register("pathfinder.command.visualizer.create")
        register("pathfinder.command.visualizer.delete")
        register("pathfinder.command.visualizer.info")
        register("pathfinder.command.visualizer.set_name")
        register("pathfinder.command.visualizer.set_permission")
        register("pathfinder.command.visualizer.set_interval")
        register("pathfinder.command.visualizer.edit")
        register("pathfinder.admin") {
            children = listOf(
                    "pathfinder.command.pathfinder.*",
                    "pathfinder.command.roadmap.*",
                    "pathfinder.command.nodegroup.*",
                    "pathfinder.command.waypoint.*",
                    "pathfinder.command.visualizer.*"
            )
        }
    }
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
    runServer {
        minecraftVersion(minecraftVersion)
    }
    shadowJar {

        archiveFileName.set("PathFinder-${parent?.version}.jar")
        mergeServiceFiles()
        mergeServiceFiles {
            path = "META-INF/extensions.idx"
        }

        project.configurations.getByName("legacyLibs").files.forEach { file ->
            from(zipTree(file))
        }

        dependencies {
            include(project(":pathfinder-api"))
            include(project(":pathfinder-core"))
            include(project(":pathfinder-graph"))
            include(project(":pathfinder-editmode"))
            include(project(":pathfinder-scripted-visualizer"))
            include(project(":vendor-disposables-api"))
            include(project(":vendor-disposables-bukkit"))
            include(dependency("org.bstats:.*"))
            include(dependency("xyz.xenondevs:particle:.*"))
            include(dependency("dev.jorel:commandapi-paper-shade:.*"))
            include(dependency("de.exlll:configlib-yaml:.*"))
            include(dependency("de.exlll:configlib-core:.*"))
            include(dependency("org.flywaydb:flyway-core:.*"))
            include(dependency("org.pf4j:pf4j:.*"))
        }

        fun relocateLib(from: String, to: String) {
            relocate(from, "de.cubbossa.pathfinder.lib.$to")
        }

        relocateLib("org.bstats", "bstats")
        relocateLib("xyz.xenondevs.particle", "particle")
        relocateLib("dev.jorel.commandapi", "commandapi")
        relocateLib("de.cubbossa.translations", "translations")
        relocateLib("de.cubbossa.splinelib", "splinelib")
        relocateLib("de.cubbossa.disposables", "disposables")
        relocateLib("de.exlll", "exlll")
        relocateLib("org.flywaydb", "flywaydb")
        relocateLib("org.pf4j", "pf4j")
    }
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

publishing {
    repositories {
        maven {
            name = "Nexus"
            url = uri("https://nexus.leonardbausenwein.de/repository/maven-public/")
            credentials {
                username = "admin"
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
    publications.create<MavenPublication>("maven") {
        groupId = rootProject.group.toString()
        artifactId = name
        version = rootProject.version.toString()

        from(components["java"])
    }
}
