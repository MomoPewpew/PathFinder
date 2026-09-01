plugins {
    `java-library`
}

group = "de.cubbossa"
version = "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
}

sourceSets {
    main {
        java.srcDirs("src/main/java")
    }
}
