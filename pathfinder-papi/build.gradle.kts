plugins {
    java
    id("io.freefair.lombok") version "8.14.4"
    id("com.gradleup.shadow") version "9.4.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {

    compileOnly(project(":pathfinder-bukkit"))
    compileOnly(project(mapOf("path" to ":vendor-legacy-libs", "configuration" to "translationsLibs")))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    annotationProcessor("com.google.auto.service:auto-service:1.0-rc5")
    implementation("com.google.auto.service:auto-service:1.0")

    compileOnly("me.clip:placeholderapi:2.11.6")
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
        fun relocateLib(from: String, to: String) {
            relocate(from, "de.cubbossa.pathfinder.lib.$to")
        }

        relocateLib("org.openjdk.nashorn", "nashorn")
    }
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
