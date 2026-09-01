plugins {
    `java-library`
    id("io.freefair.lombok") version "8.14.4"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:24.0.0")

    api("com.google.guava:guava:32.1.2-jre")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
