plugins {
    kotlin("jvm") version "1.9.22"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.obermuhlner:big-math:2.3.2")
}

kotlin {
    jvmToolchain(17)
}
