import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
	toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

// --- 1. GESTION DES SOURCES (ARBORESCENCE) ---
sourceSets {
	// Suite pour les tests d'intégration (Spring + BDD)
	create("testIntegration") {
		compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		runtimeClasspath += output + compileClasspath
	}
	// Suite pour les tests de composants (Cucumber / Gherkin)
	create("testComponent") {
		compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		runtimeClasspath += output + compileClasspath
	}
}

// Configuration des dépendances pour les nouvelles suites
val testIntegrationImplementation by configurations.getting { extendsFrom(configurations.testImplementation.get()) }
val testComponentImplementation by configurations.getting {
	extendsFrom(configurations.testImplementation.get())
}

repositories {
	mavenCentral()
}

// --- 2. DÉPENDANCES ---
dependencies {
	// Spring Boot & Kotlin
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// Base de données
	implementation("org.postgresql:postgresql")
	implementation("org.liquibase:liquibase-core")


	"testComponentImplementation"("org.testcontainers:postgresql:1.19.1")
	"testComponentImplementation"("org.testcontainers:testcontainers:1.19.1")

	// --- TESTS UNITAIRES (JUnit 5 + Kotest + Mockk) ---
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.mockk:mockk:1.13.10")
	testImplementation("io.kotest:kotest-property:5.9.1")

	// --- TESTS D'INTÉGRATION (Testcontainers + SpringMockk) ---
	testIntegrationImplementation("org.testcontainers:postgresql:1.19.1")
	testIntegrationImplementation("com.ninja-squad:springmockk:4.0.2")
	testIntegrationImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

	// --- TESTS DE COMPOSANTS (Cucumber + RestAssured) ---
	val cucumberVersion = "7.14.0"
	testComponentImplementation("io.cucumber:cucumber-java:$cucumberVersion")
	testComponentImplementation("io.cucumber:cucumber-spring:$cucumberVersion")
	testComponentImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
	testComponentImplementation("io.rest-assured:rest-assured:5.3.2")
	testComponentImplementation("org.junit.platform:junit-platform-suite:1.10.0")
}



// --- 3. CONFIGURATION DES TÂCHES DE TEST ---

// Tâche pour l'intégration
val testIntegration = tasks.register<Test>("testIntegration") {
	description = "Lance les tests d'intégration (BDD & Controllers)."
	group = "verification"
	testClassesDirs = sourceSets["testIntegration"].output.classesDirs
	classpath = sourceSets["testIntegration"].runtimeClasspath
	useJUnitPlatform()
}

// Tâche pour les composants (Cucumber)
val testComponent = tasks.register<Test>("testComponent") {
	description = "Lance les tests fonctionnels Cucumber."
	group = "verification"
	testClassesDirs = sourceSets["testComponent"].output.classesDirs
	classpath = sourceSets["testComponent"].runtimeClasspath
	useJUnitPlatform()
}

// Configuration commune à tous les tests
tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
}

// --- 4. QUALITÉ & RAPPORTS ---

kotlin {
	compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
}

tasks.jacocoTestReport {
	dependsOn(tasks.test, testIntegration) // JaCoCo analyse les deux suites
	reports {
		html.required.set(true)
		xml.required.set(true)
	}
}

pitest {
	targetClasses.set(listOf("livres.domain.*"))
	targetTests.set(listOf("livres.domain.*"))
	outputFormats.set(listOf("HTML", "XML"))
	timestampedReports.set(false)
}