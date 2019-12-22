
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `build-scan`
    `maven-publish`
    kotlin("jvm") version "1.2.71"
    id("org.jetbrains.dokka") version "0.9.17"
}

group = "com.etesync"
version = "1.0.1"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    val spongyCastleVersion = "1.54.0.0"
    implementation("com.madgag.spongycastle:core:$spongyCastleVersion")
    implementation("com.madgag.spongycastle:prov:$spongyCastleVersion")

    val okhttp3Version = "3.12.1"
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttp3Version")

    implementation("com.google.code.gson:gson:1.7.2")
    implementation("org.apache.commons:commons-collections4:4.1")
    implementation("org.apache.commons:commons-lang3:3.8.1")
    implementation("commons-codec:commons-codec:1.7")

    testImplementation("junit:junit:4.12")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttp3Version")
}

// Configure existing Dokka task to output HTML to typical Javadoc directory
tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

// Create dokka Jar task from dokka task output
val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    // dependsOn(tasks.dokka) not needed; dependency automatically inferred by from(tasks.dokka)
    from(tasks.dokka)
}

// Create sources Jar from main kotlin sources
val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    classifier = "sources"
    from(project.the<SourceSetContainer>()["main"].allSource)
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifact(sourcesJar)
            artifact(dokkaJar)
        }
    }
    repositories {
        maven {
            url = uri("$buildDir/repository")
        }
    }
}
