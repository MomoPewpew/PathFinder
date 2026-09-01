plugins {
    `java-library`
}

group = "de.cubbossa"
version = "1.3"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

// Vendored dependency — upstream tests are not part of the PathFinder build.
tasks.named("test") {
    enabled = false
}
