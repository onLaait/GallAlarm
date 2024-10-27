plugins {
    kotlin("jvm") version "1.9.25"
    id("com.gradleup.shadow") version "8.3.3"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")

    implementation("be.zvz:KotlinInside:1.16.2")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")

    implementation("org.jsoup:jsoup:1.18.1")
    implementation("org.apache.commons:commons-text:1.12.0")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
        manifest {
            attributes (
                "Main-Class" to "com.github.onlaait.gallalarm.MainKt",
                "Multi-Release" to true
            )
        }
    }
}

kotlin {
    jvmToolchain(21)
}