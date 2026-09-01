plugins {
    `java-library`
}

group = "de.cubbossa"
version = "1.3"

val paperApiVersion = project.property("paper_api_version") as String

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
    api(project(":vendor-disposables-api"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
}
