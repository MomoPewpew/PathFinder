plugins {
    `java-library`
    id("io.freefair.lombok") version "8.14.4"
}

group = "de.cubbossa"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(project(":pathfinder-graph"))
    api(project(":vendor-disposables-api"))
    implementation("org.jetbrains:annotations:24.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    api(platform("net.kyori:adventure-bom:5.2.0"))
    api("net.kyori:adventure-api")
    compileOnlyApi("io.github.toxicity188:adventure-platform-bukkit:5.2.0")
    api("net.kyori:adventure-text-minimessage")
    api("net.kyori:adventure-text-serializer-plain")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
