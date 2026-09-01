plugins {
    id("java")
}

val paperApiVersion = project.property("paper_api_version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net")
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("me.pikamug.quests:quests-core:5.0.5")
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    implementation(project(":pathfinder-api"))
    implementation(project(":pathfinder-bukkit"))

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
