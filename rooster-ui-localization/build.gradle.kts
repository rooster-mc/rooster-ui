plugins {
    kotlin("jvm") version "2.2.0"
}

group = "dev.rooster.ui.localization"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")

    implementation("dev.rooster.core:rooster-core:1.0-SNAPSHOT")
    implementation("dev.rooster.ui:RoosterUI:1.0-SNAPSHOT")
    implementation("dev.rooster.localization:rooster-localization:1.0-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
