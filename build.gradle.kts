import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val INTEGRATION_TEST = "integrationTest"

plugins {
    id("org.springframework.boot") version "3.0.4"
    id("io.spring.dependency-management") version "1.1.0"
    id("com.google.cloud.tools.jib") version "3.3.1"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
}

group = "com.patrykglow"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

sourceSets {
    create(INTEGRATION_TEST) {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2022.0.1"
extra["googleOauthClient"] = "1.34.1"
extra["kotestVersion"] = "5.5.5"
extra["resilience4jVersion"] = "2.0.2"
extra["springCloudContractWiremockVersion"] = "4.0.1"

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("io.github.resilience4j:resilience4j-kotlin:${property("resilience4jVersion")}")
    implementation("io.github.resilience4j:resilience4j-retry:${property("resilience4jVersion")}")
    implementation("io.github.resilience4j:resilience4j-timelimiter:${property("resilience4jVersion")}")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest:kotest-runner-junit5:${property("kotestVersion")}")

    integrationTestImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:${property("springCloudContractWiremockVersion")}")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

val integrationTest = task<Test>(INTEGRATION_TEST) {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets[INTEGRATION_TEST].output.classesDirs
    classpath = sourceSets[INTEGRATION_TEST].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform()

    testLogging {
        events("passed")
    }
}

tasks.check { dependsOn(integrationTest) }

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}

jib.to {
    image = "patryk.glow/twitter-connector:" + System.nanoTime()
}

