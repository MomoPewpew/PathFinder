plugins {
    `java-library`
    id("io.freefair.lombok") version "8.14.4"
}

val paperApiVersion = project.property("paper_api_version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://maven.citizensnpcs.co/repo")
    maven("https://libraries.minecraft.net")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {

    compileOnly(project(":pathfinder-bukkit"))
    testImplementation(project(":pathfinder-bukkit"))
    implementation(project(":vendor-disposables-api"))

    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")

    compileOnly("net.citizensnpcs:citizens-main:2.0.43-SNAPSHOT") {
        exclude(group = "*", module = "*")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
