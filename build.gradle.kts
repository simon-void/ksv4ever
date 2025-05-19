
plugins {
    kotlin("jvm") version "2.2.0-RC"
    `java-library`
}

group = "net.simonvoid"
version = "2.0.0"

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        extraWarnings.set(true)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

//    fun kotlinx(module: String) = "org.jetbrains.kotlinx:kotlinx-$module"
//    implementation(kotlinx("io-core-jvm:0.7.0"))
//    implementation(kotlinx("datetime-jvm:0.6.2"))

    testImplementation("org.testng:testng:7.11.0")
    testImplementation("io.mockk:mockk:1.14.2")
}

tasks {
    withType<Test> {
        useTestNG()
    }

    val sourcesJar = register<Jar>("sourcesJar") {
        archiveClassifier = "sources"
        from(sourceSets.main.get().allSource)
    }

    // TODO: generate javadoc via Dokka
    // wait for Dokka to reach 2.1.0 (so that the migration of the Dokka plugin to Version2 is finished)
    // https://github.com/Kotlin/dokka
    // https://kotlinlang.org/docs/dokka-gradle.html

    artifacts {
        archives(sourcesJar)
        archives(jar)
    }
}
