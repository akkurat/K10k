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

val mc = "ch.taburett.cli.CliKt"
application {
    mainClass = mc
}
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = mc
    }
}
tasks.withType<Jar> {
    // https://www.baeldung.com/gradle-fat-jar
    // https://stackoverflow.com/questions/46157338/using-gradle-to-build-a-jar-with-dependencies-with-kotlin-dsl
    configurations["runtimeClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile)) {
            duplicatesStrategy = DuplicatesStrategy.WARN
        }
    }
}

val battleMainClass = "ch.taburett.battle.BattleKt"
// not figured out where to properly get a second jar as ouptu
tasks.register<Jar>("jarBattle") {
    manifest {
        attributes["Main-Class"] = battleMainClass
    }
    archiveBaseName = "battle"
    from(sourceSets.main.get().output) {
//        // thinking about it this could have been solved by submodules
//        // but at least learned a bit more gradle
//        seems that jar task has implicit build output included but type jar not
        include(
            "ch/taburett/common/**",
            "ch/taburett/battle/**",
        )
    }
}


tasks.register<JavaExec>("runBattle") {
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets.main.get().output
    mainClass = battleMainClass
}
