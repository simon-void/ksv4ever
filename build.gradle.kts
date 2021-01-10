import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "1.4.21"
    id("java-library")
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.4"
}

group = "net.simonvoid"
version = "1.1.3"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = java.sourceCompatibility

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

val artifactName = rootProject.name
val artifactGroup = project.group.toString()
val artifactVersion = project.version.toString()

val pomDeveloperId = "simon-void"
val pomDeveloperName = "Stephan Schroeder"

val githubRepo = "$pomDeveloperId/$artifactName"
val githubReadme = "README.md"

val pomUrl = "https://github.com/$githubRepo"
val pomScmUrl = pomUrl
val pomIssueUrl = "$pomUrl/issues"
val pomDesc = pomUrl

val pomLicenseName = "MIT"
val pomLicenseUrl = "https://opensource.org/licenses/mit-license.php"
val pomLicenseDist = "repo"

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn.add(tasks.javadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

publishing {
    publications {
        create<MavenPublication>(artifactName) {
            groupId = artifactGroup
            artifactId = artifactName
            version = artifactVersion
            from(components["java"])

            artifacts {
                archives(sourcesJar)
                archives(javadocJar)
                archives(tasks.jar)
            }

            pom.withXml {
                asNode().apply {
                    appendNode("description", pomDesc)
                    appendNode("name", artifactName)
                    appendNode("url", pomUrl)
                    appendNode("licenses").appendNode("license").apply {
                        appendNode("name", pomLicenseName)
                        appendNode("url", pomLicenseUrl)
                        appendNode("distribution", pomLicenseDist)
                    }
                    appendNode("developers").appendNode("developer").apply {
                        appendNode("id", pomDeveloperId)
                        appendNode("name", pomDeveloperName)
                    }
                    appendNode("scm").apply {
                        appendNode("url", pomScmUrl)
                    }
                }
            }
        }
    }
}

// upload to Bintray via
// ./gradlew bintrayUpload -PbintrayUser="my_bintray_username" -PbintrayKey="my_bintray_api_key"

bintray {
    user = project.findProperty("bintrayUser").toString()
    key = project.findProperty("bintrayKey").toString()
    publish = true

    val repoName = "maven"

    setPublications(artifactName)

    pkg.apply {
        repo = repoName
        name = artifactName
        userOrg = user.toLowerCase()
        githubRepo = githubRepo
        vcsUrl = pomScmUrl
        description = "robust mapping of csv data to user-defined Kotlin data classes"
        setLabels("kotlin", "csv")
        setLicenses("MIT")
        desc = description
        websiteUrl = pomUrl
        issueTrackerUrl = pomIssueUrl
        githubReleaseNotesFile = githubReadme

        version.apply {
            name = artifactVersion
            desc = pomDesc
            released = LocalDateTime.now().let { now->
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                now.format(formatter)
            }
            vcsTag = artifactVersion
        }
    }
}
