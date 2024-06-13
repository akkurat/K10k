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

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")

    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.23.1")

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

    // https://www.baeldung.com/gradle-fat-jar
    // https://stackoverflow.com/questions/46157338/using-gradle-to-build-a-jar-with-dependencies-with-kotlin-dsl
    configurations["runtimeClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}