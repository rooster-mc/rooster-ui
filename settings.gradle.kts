plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "RoosterUI"

include(":RoosterCore")
project(":RoosterCore").projectDir = file("../rooster-core")

include(":RoosterLocalization")
project(":RoosterLocalization").projectDir = file("../rooster-localization")

include("DemoPlugin")
