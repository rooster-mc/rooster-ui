plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "RoosterUI"

includeBuild("../rooster-core")
includeBuild("../rooster-localization")
includeBuild("demo-plugin")
includeBuild("rooster-ui-sql")
