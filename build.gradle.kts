import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    `maven-publish`
    kotlin("multiplatform") version "1.4.20"
    kotlin("plugin.spring") version "1.4.20"
    id("org.springframework.boot") version "2.3.1.RELEASE"
}

repositories {
    jcenter()
}

val kotlinVersion = "1.4.20"
val diktatVersion = "0.1.3"
val ktlintVersion = "0.39.0"
val springBootVersion = "2.3.1.RELEASE"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "ord.cqfn.diktat"
            version = project.version as String
            description = "diktat-demo"
            from(components["java"])
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

kotlin {
    js {
        browser { }
    }

    jvm {
        repositories {
            mavenLocal()
            mavenCentral()
        }
        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        getByName("jvmMain") {
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter-thymeleaf:$springBootVersion")
                implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
                implementation("org.cqfn.diktat:diktat-common:$diktatVersion") {
                    // exclude to use logback provided by spring
                    exclude("org.slf4j", "slf4j-log4j12")
                }
                implementation("org.cqfn.diktat:diktat-rules:$diktatVersion") {
                    exclude("org.slf4j", "slf4j-log4j12")
                }
                implementation("com.pinterest.ktlint:ktlint-core:$ktlintVersion")
                implementation("com.pinterest.ktlint:ktlint-ruleset-standard:$ktlintVersion")
                implementation(kotlin("stdlib-jdk8"))
            }
        }

        getByName("jvmTest") {
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
            }
        }

        getByName("jsMain") {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation(npm("ace-builds", "1.4.11"))
            }
        }
    }
}

tasks.getByName("jvmMainClasses").dependsOn(tasks.getByName("processDceJsKotlinJs"))
tasks.getByName("jvmMainClasses") {
    doLast {
        mkdir("build/processedResources/jvm/main/static/js")
        copy {
            from("build/js/packages/diktat-demo/kotlin-dce")
            include("*.js", "*.js.map")
            into("build/processedResources/jvm/main/static/js")
        }
    }
}

tasks.getByName<BootJar>("bootJar") {
    requiresUnpack("**/kotlin-compiler-embeddable-*.jar")
}

val ktlint: Configuration by configurations.creating
val outputDir = "${project.buildDir}/reports/diktat/"
val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))
val diktatCheck by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"

    // specify proper path to sources that should be checked here
    args = listOf("src/*/kotlin/**/*.kt")
    dependencies {
        ktlint("com.pinterest:ktlint:$ktlintVersion") {
            // need to exclude standard ruleset to use only diktat rules
            exclude("com.pinterest.ktlint", "ktlint-ruleset-standard")
        }

        // diktat ruleset
        ktlint("org.cqfn.diktat:diktat-rules:$diktatVersion")
    }
}
