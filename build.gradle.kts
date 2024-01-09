plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1")
    implementation("be.zvz:KotlinInside:1.16.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    implementation("org.jsoup:jsoup:1.17.2")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
        manifest {
            attributes (
                "Main-Class" to "com.github.onlaait.gallalarm.MainKt",
                "Multi-Release" to true
            )
        }
        minimize {
            exclude(dependency("org.apache.logging.log4j:.*:.*"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

kotlin {
    jvmToolchain(17)
}