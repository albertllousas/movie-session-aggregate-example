plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
}

object Versions {
    const val JUNIT = "5.8.2"
    const val MOCKK = "1.12.0"
    const val ASSERTJ = "3.20.2"
    const val ARROW = "1.1.2"
    const val FAKER = "1.0.2"
    const val APACHE_COMMONS_LANG = "3.12.0"
}

repositories {
    google()
    mavenCentral()
}
dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.arrow-kt:arrow-core:${Versions.ARROW}")
    implementation("org.apache.commons:commons-lang3:${Versions.APACHE_COMMONS_LANG}")

    testImplementation(group = "io.mockk", name = "mockk", version = Versions.MOCKK)
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}")
    testImplementation(group = "org.assertj", name = "assertj-core", version = Versions.ASSERTJ)
}

tasks.apply {
    test {
        maxParallelForks = 1
        enableAssertions = true
        useJUnitPlatform {}
    }
}