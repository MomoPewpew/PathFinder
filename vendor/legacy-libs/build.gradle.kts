plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.4.1"
}

group = "de.cubbossa"
version = "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

val pathFinderJar = rootProject.file("libs/PathFinder-5.4.2.jar")

tasks {
    val translationsJar = register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("translationsJar") {
        archiveClassifier.set("")
        archiveBaseName.set("cubbossa-translations")

        from(zipTree(pathFinderJar)) {
            include("de/cubbossa/pathfinder/lib/translations/**")
            include("README.txt")
        }

        relocate("de.cubbossa.pathfinder.lib.translations", "de.cubbossa.translations")
        relocate("de.cubbossa.pathfinder.lib.kyori", "net.kyori")
    }

    val menuframeworkJar = register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("menuframeworkJar") {
        archiveClassifier.set("")
        archiveBaseName.set("cubbossa-menuframework")

        from(zipTree(pathFinderJar)) {
            include("de/cubbossa/pathfinder/lib/gui/**")
        }

        relocate("de.cubbossa.pathfinder.lib.gui", "de.cubbossa.menuframework")
        relocate("de.cubbossa.pathfinder.lib.kyori", "net.kyori")
    }

    val splinelibJar = register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("splinelibJar") {
        archiveClassifier.set("")
        archiveBaseName.set("cubbossa-splinelib")

        from(zipTree(pathFinderJar)) {
            include("de/cubbossa/pathfinder/lib/splinelib/**")
        }

        relocate("de.cubbossa.pathfinder.lib.splinelib", "de.cubbossa.splinelib")
    }

    build {
        dependsOn(translationsJar, menuframeworkJar, splinelibJar)
    }
}

configurations {
    create("translationsLibs") {
        isCanBeConsumed = true
        isCanBeResolved = false
    }
    create("menuframeworkLibs") {
        isCanBeConsumed = true
        isCanBeResolved = false
    }
    create("splinelibLibs") {
        isCanBeConsumed = true
        isCanBeResolved = false
    }
}

artifacts {
    add("translationsLibs", tasks.named("translationsJar"))
    add("menuframeworkLibs", tasks.named("menuframeworkJar"))
    add("splinelibLibs", tasks.named("splinelibJar"))
}
