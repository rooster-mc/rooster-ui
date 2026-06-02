import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.2.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "8.3.3"
}

group = "dev.rooster.ui.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:10.1.2")
    //implementation("dev.jorel:commandapi-bukkit-kotlin-shade-mojang-mapped:10.1.2")

    implementation("dev.rooster.core:rooster-core:1.0-SNAPSHOT")
    implementation("dev.rooster.ui:RoosterUI:1.0-SNAPSHOT")
}
tasks {
    runServer {
        minecraftVersion("1.21.5") // set explicitly if not auto-resolved
        jvmArgs("-Dkotlinx.coroutines.debug=off")
    }
}

tasks.withType<ShadowJar> {
    relocate("dev.jorel.commandapi", "dev.rooster.ui.demo.commandapi")
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
