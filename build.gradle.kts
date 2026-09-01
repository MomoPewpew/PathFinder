plugins {
    idea
    java
    eclipse
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

group = "de.cubbossa"
version = "5.4.2"

subprojects {
    if (path.startsWith(":vendor-")) {
        return@subprojects
    }

    apply {
        plugin("java")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    repositories {
        mavenCentral()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven {
            url = uri("https://nexus.leonardbausenwein.de/repository/maven-public/")
            content {
                includeGroupByRegex("de\\.cubbossa")
            }
        }
    }

    dependencies {
        implementation("org.pf4j:pf4j:3.11.0")
        annotationProcessor("org.pf4j:pf4j:3.11.0")
        testImplementation("org.pf4j:pf4j:3.11.0")

        testAnnotationProcessor("org.pf4j:pf4j:3.11.0")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        testImplementation("com.pholser:junit-quickcheck-core:1.0")
        testImplementation("com.pholser:junit-quickcheck-generators:1.0")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}