plugins {
    kotlin("multiplatform") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

group = "org.jetbrains.kotlin-wrappers"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(LEGACY) {
        binaries.executable()
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.benasher44:uuid:0.3.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:1.6.7")
                implementation("io.ktor:ktor-serialization:1.6.7")
                implementation("io.ktor:ktor-auth:1.6.7")
                implementation("io.ktor:ktor-auth-jwt:1.6.7")
                implementation("io.ktor:ktor-html-builder:1.6.7")

                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
                implementation("ch.qos.logback:logback-classic:1.2.9")

                implementation("org.jetbrains.exposed:exposed-core:0.36.2")
                implementation("org.jetbrains.exposed:exposed-jdbc:0.36.2")

                implementation("com.h2database:h2:1.4.200")
                implementation("com.zaxxer:HikariCP:5.0.0")
                implementation("org.flywaydb:flyway-core:8.3.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.ktor:ktor-server-test-host:1.6.7")
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("org.jetbrains.kotlin.wrappers.realworld.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}
