plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

val mc = "ch.taburett.MainKt"
application {
    mainClass = mc
}
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = mc
    }
    configurations["runtimeClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}