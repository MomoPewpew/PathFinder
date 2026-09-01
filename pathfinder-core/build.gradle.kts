import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.ForcedType

plugins {
    antlr
    `java-library`
    `maven-publish`
    `java-test-fixtures`
    id("com.gradleup.shadow") version "9.4.1"
    id("io.freefair.lombok") version "8.14.4"
    id("nu.studer.jooq") version "10.2.1"
}

val paperApiVersion = project.property("paper_api_version") as String
val commandApiVersion = project.property("commandapi_version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-snapshots/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven {
        url = uri("https://nexus.leonardbausenwein.de/repository/maven-public/")
        content {
            includeGroupByRegex("de\\.cubbossa")
        }
    }
}

dependencies {

    antlr("org.antlr:antlr4:4.12.0") { isTransitive = true }

    api("de.exlll:configlib-yaml:4.5.0")

    api("dev.jorel:commandapi-paper-shade:$commandApiVersion")

    api("org.jooq:jooq:3.16.23")
    jooqGenerator("org.xerial:sqlite-jdbc:3.41.2.1")
    implementation("org.xerial:sqlite-jdbc:3.41.2.1")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("io.r2dbc:r2dbc-spi:1.0.0.RELEASE")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation(project(":vendor-disposables-api"))
    api("org.flywaydb:flyway-core:8.0.0")
    compileOnly("org.jetbrains:annotations:24.0.1")

    compileOnly(project(":vendor-legacy-libs", configuration = "splinelibLibs"))
    compileOnly(project(":vendor-legacy-libs", configuration = "translationsLibs"))
    testImplementation(project(":vendor-legacy-libs", configuration = "splinelibLibs"))
    testImplementation(project(":vendor-legacy-libs", configuration = "translationsLibs"))

    api(project(":pathfinder-api"))
    api(project(":pathfinder-graph"))

    implementation("org.bstats:bstats-bukkit:3.0.1")

    compileOnlyApi("io.papermc.paper:paper-api:$paperApiVersion")
    testImplementation("io.papermc.paper:paper-api:$paperApiVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.xerial:sqlite-jdbc:3.41.2.1")
    testImplementation("com.h2database:h2:2.1.214")
    testImplementation(project(":pathfinder-test-utils"))

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.6")
    implementation(files("generated/plugin-yml/Bukkit/plugin.yml"))
}

tasks {
    generateGrammarSource {
        arguments.plusAssign("-visitor")
    }
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

sourceSets {
    main {
        java.srcDirs += file("build/generated-src/antlr/main")
        resources {
            exclude("*.db")
        }
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

jooq {
    version.set("3.16.23")
    edition.set(JooqEdition.OSS)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    url = "jdbc:sqlite:src/main/resources/database_template.db"
                }
                generator.apply {
                    name = "org.jooq.codegen.JavaGenerator"
                    database.apply {
                        name = "org.jooq.meta.sqlite.SQLiteDatabase"
                        forcedTypes = listOf(
                            ForcedType().apply {
                                userType = "de.cubbossa.pathfinder.misc.NamespacedKey"
                                converter =
                                    "de.cubbossa.pathfinder.storage.misc.NamespacedKeyConverter"
                                includeExpression = ".*key|.*type"
                            },
                            ForcedType().apply {
                                userType = "java.util.UUID"
                                converter = "de.cubbossa.pathfinder.storage.misc.UUIDConverter"
                                includeExpression = "id|.*_id|world"
                            }
                        )
                    }
                    target.apply {
                        directory = "src/main/jooq"
                        packageName = "de.cubbossa.pathfinder.jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}
