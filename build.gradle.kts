plugins {
	// --- Framework & Language ---
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"

	// --- Quality & Tests (Étape 6 & 7) ---
	jacoco
	id("info.solidsoft.pitest") version "1.19.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// --- Production ---
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation(kotlin("stdlib-jdk8"))

	// --- Testing ---
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// Kotest : On s'assure d'avoir le moteur Junit5 de Kotest
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.kotest:kotest-property:5.9.1")

	testImplementation("io.mockk:mockk:1.13.8")

	// FORCE le launcher pour Gradle 9
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")

	pitest("org.pitest:pitest-junit5-plugin:1.2.1")
}
// --- Configuration Kotlin ---
kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

// --- Configuration des Tests Junit ---
tasks.withType<Test> {
	useJUnitPlatform {
		// Force l'utilisation du moteur Kotest
		includeEngines("kotest")
	}

	systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

	testLogging {
		events("passed", "skipped", "failed")
		showExceptions = true
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
	}
}
// --- Étape 6/7 : Couverture de code (JaCoCo) ---
tasks.jacocoTestReport {
	dependsOn(tasks.test) // On génère le rapport seulement après les tests
	reports {
		xml.required.set(true) // Indispensable pour GitHub Actions
		html.required.set(true)
	}
}

// --- Étape 7/7 : Tests de mutation (PITest) ---
pitest {
	targetClasses.set(listOf("livres.domain.*"))
	targetTests.set(listOf("livres.domain.*"))

	jvmArgs.set(listOf(
		"-Djunit.jupiter.extensions.autodetection.enabled=true",
		// Option indispensable pour Java 21+ afin d'autoriser la réflexion profonde
		"--add-opens", "java.base/java.lang=ALL-UNNAMED",
		"-XX:+AllowRedefinitionToAddDeleteMethods"
	))

	useClasspathFile.set(true)
	threads.set(1) // Garder 1 thread aide à stabiliser sur Ubuntu

	outputFormats.set(listOf("HTML", "XML"))
	timestampedReports.set(false)
	mutationThreshold.set(0)
}