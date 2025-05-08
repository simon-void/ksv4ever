
plugins {
    kotlin("jvm") version "2.1.20"
    `java-library`
}

group = "net.simonvoid"
version = "2.0.0"

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        run {
            extraWarnings.set(true)
            // suppress specific extra warning, because of https://youtrack.jetbrains.com/issue/KT-73736
            // might be fixed by now
            freeCompilerArgs.add("-Xsuppress-warning=UNUSED_ANONYMOUS_PARAMETER")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

    testImplementation("org.testng:testng:7.11.0")
    testImplementation("io.mockk:mockk:1.14.2")
}

tasks {
    withType<Test> {
        useTestNG()
    }

    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn.add(javadoc)
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    artifacts {
        archives(sourcesJar)
        archives(javadocJar)
        archives(jar)
    }
}
