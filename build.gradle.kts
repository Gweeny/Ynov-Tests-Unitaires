import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm") version "2.0.21"
	kotlin("plugin.spring") version "2.0.21"
	id("org.springframework.boot") version "3.3.0"
	id("io.spring.dependency-management") version "1.1.5"
	jacoco
	id("info.solidsoft.pitest") version "1.19.0"
	id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

// --- 1. GESTION DES SOURCES ---
sourceSets {
	create("testIntegration") {
		compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		runtimeClasspath += output + compileClasspath
	}
	create("testComponent") {
		compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		runtimeClasspath += output + compileClasspath
	}
	create("testArchitecture") {
		compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		runtimeClasspath += output + compileClasspath
	}
}

// Configuration des héritages pour éviter les "Unresolved reference" sur GitHub Actions
configurations {
	val testImplementation = testImplementation.get()
	val testRuntimeOnly = testRuntimeOnly.get()

	getByName("testIntegrationImplementation") { extendsFrom(testImplementation) }
	getByName("testComponentImplementation") { extendsFrom(testImplementation) }
	getByName("testArchitectureImplementation") { extendsFrom(testImplementation) }
}

repositories {
	mavenCentral()
}

// --- 2. DÉPENDANCES ---
dependencies {
	// --- APP ---
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.postgresql:postgresql")
	implementation("org.liquibase:liquibase-core")

	// --- BASE DE TESTS COMMUNE (Appliquée à toutes les sourceSets) ---
	val testLib = listOf(
		"org.springframework.boot:spring-boot-starter-test",
		"io.kotest:kotest-runner-junit5:5.9.1",
		"io.kotest:kotest-assertions-core:5.9.1",
		"io.kotest:kotest-property:5.9.1",
		"io.kotest.extensions:kotest-extensions-spring:1.1.3",
		"io.mockk:mockk:1.13.10",
		"com.ninja-squad:springmockk:4.0.2"
	)

	testLib.forEach {
		testImplementation(it)
		"testIntegrationImplementation"(it)
		"testComponentImplementation"(it)
		"testArchitectureImplementation"(it)
	}

	// --- 1. TESTS D'INTÉGRATION (Spécifique) ---
	"testIntegrationImplementation"("org.testcontainers:postgresql:1.19.1")

	// --- 2. TESTS DE COMPOSANTS (Cucumber + Fix Testcontainers) ---
	// Cette ligne corrige l'erreur "Unresolved reference testcontainers" dans src/testComponent
	"testComponentImplementation"("org.testcontainers:postgresql:1.19.1")

	val cucumberVersion = "7.14.0"
	"testComponentImplementation"("io.cucumber:cucumber-java:$cucumberVersion")
	"testComponentImplementation"("io.cucumber:cucumber-spring:$cucumberVersion")
	"testComponentImplementation"("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
	"testComponentImplementation"("org.junit.platform:junit-platform-suite:1.10.0")
	"testComponentImplementation"("io.rest-assured:rest-assured:5.3.2")
	"testComponentImplementation"("io.rest-assured:kotlin-extensions:5.3.2")

	// --- 3. TESTS D'ARCHITECTURE ---
	"testArchitectureImplementation"("com.tngtech.archunit:archunit-junit5:1.0.1")

	// --- 4. QUALITÉ ---
	detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.23.7")
	detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}

// --- 3. TÂCHES DE TEST ---
tasks.withType<Test> {
	useJUnitPlatform()
}

val testIntegration = tasks.register<Test>("testIntegration") {
	group = "verification"
	testClassesDirs = sourceSets["testIntegration"].output.classesDirs
	classpath = sourceSets["testIntegration"].runtimeClasspath
}

val testComponent = tasks.register<Test>("testComponent") {
	group = "verification"
	testClassesDirs = sourceSets["testComponent"].output.classesDirs
	classpath = sourceSets["testComponent"].runtimeClasspath
}

val testArchitecture = tasks.register<Test>("testArchitecture") {
	group = "verification"
	testClassesDirs = sourceSets["testArchitecture"].output.classesDirs
	classpath = sourceSets["testArchitecture"].runtimeClasspath
}

// --- 4. QUALITÉ & DETEKT (MODE CLI ISOLÉ) ---
val detektCli by configurations.creating
dependencies {
	detektCli("io.gitlab.arturbosch.detekt:detekt-cli:1.23.7")
	detektCli("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}

tasks.named("detekt") { enabled = false }

tasks.register<JavaExec>("detektCheck") {
	group = "verification"
	mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
	classpath = detektCli
	args(
		"--input", projectDir.absolutePath,
		"--config", file("config/detekt.yml").absolutePath,
		"--report", "html:${layout.buildDirectory.get().asFile}/reports/detekt/detekt.html"
	)
}

// --- 5. RAPPORTS & COUVERTURE ---
tasks.jacocoTestReport {
	dependsOn(tasks.test, testIntegration, testComponent)
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

kotlin {
	compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
}