plugins {
    id("java")
    id("io.freefair.lombok") version "8.14.4"
}

group = "de.cubbossa"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(project(":pathfinder-api"))
    compileOnly(project(":pathfinder-core"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    implementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
    implementation("com.h2database:h2:2.1.214")
}

tasks.test {
    useJUnitPlatform()
}
