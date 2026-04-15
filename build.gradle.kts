plugins {
	kotlin("jvm") version "2.1.10"
	kotlin("plugin.spring") version "2.1.10"
	id("org.springframework.boot") version "3.3.0"
	id("io.spring.dependency-management") version "1.1.5"
	jacoco
	id("info.solidsoft.pitest") version "1.19.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

// --- CONFIGURATION MANUELLE DU DOSSIER testIntegration ---
sourceSets {
	create("testIntegration") {
		compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
	}
}

val testIntegrationImplementation by configurations.getting {
	extendsFrom(configurations.testImplementation.get())
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-web")
	// Tests Unitaires
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.kotest:kotest-property:5.9.1")
	testImplementation("io.mockk:mockk:1.13.10")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.postgresql:postgresql")

	implementation("org.liquibase:liquibase-core")

	testIntegrationImplementation("org.testcontainers:postgresql:1.19.1")
	testIntegrationImplementation("org.testcontainers:testcontainers:1.19.1")
	testIntegrationImplementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")

// Dépendances spécifiques Intégration (On complète selon ton cours)
	"testIntegrationImplementation"("com.ninja-squad:springmockk:4.0.2")
	"testIntegrationImplementation"("io.kotest.extensions:kotest-extensions-spring:1.3.0")

	// Ajout du starter-test avec l'exclusion demandée par le cours
	"testIntegrationImplementation"("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}
}

// --- CRÉATION DE LA TÂCHE DE TEST ---
val testIntegration = tasks.register<Test>("testIntegration") {
	description = "Lance les tests d'intégration."
	group = "verification"
	testClassesDirs = sourceSets["testIntegration"].output.classesDirs
	classpath = sourceSets["testIntegration"].runtimeClasspath
	shouldRunAfter(tasks.test)
	useJUnitPlatform()
}

kotlin {
	compilerOptions {
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

pitest {
	targetClasses.set(listOf("livres.domain.*"))
	targetTests.set(listOf("livres.domain.*"))
	jvmArgs.set(listOf(
		"-Djunit.jupiter.extensions.autodetection.enabled=true",
		"--add-opens", "java.base/java.lang=ALL-UNNAMED"
	))
	useClasspathFile.set(true)
	timestampedReports.set(false)
	outputFormats.set(listOf("HTML", "XML"))
}