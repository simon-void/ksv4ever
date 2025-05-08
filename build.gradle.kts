import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.20"
}

group = "net.simonvoid"
version = "1.1.4"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = java.sourceCompatibility

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.testng:testng:7.4.0")
    testImplementation("io.mockk:mockk:1.14.2")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = java.sourceCompatibility.toString()
            kotlinOptions {
                languageVersion = "2.1"
            }
        }
    }

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
