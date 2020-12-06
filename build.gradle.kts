import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
}

group = "net.simonvoid.ksv4ever"
version = "1.1.0"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.testng:testng:7.3.0")
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = java.sourceCompatibility.toString()
            kotlinOptions {
                languageVersion = "1.4"
            }
        }
    }

    withType<Test> {
        useTestNG()
    }
}
