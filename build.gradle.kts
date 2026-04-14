plugins {
	// On monte d'un cran pour stabiliser avec Gradle 9
	kotlin("jvm") version "2.0.0"
	kotlin("plugin.spring") version "2.0.0"

	// Spring Boot 3.3.0 est plus à l'aise avec Gradle 9
	id("org.springframework.boot") version "3.3.0"
	id("io.spring.dependency-management") version "1.1.5"

	jacoco
	// On reste sur la dernière version de PIT
	id("info.solidsoft.pitest") version "1.19.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.kotest:kotest-property:5.9.1")
	testImplementation("io.mockk:mockk:1.13.10")

	// Bridge requis pour Gradle 9
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Plugin PIT pour JUnit 5
	pitest("org.pitest:pitest-junit5-plugin:1.2.1")
}

// Configuration simplifiée pour Gradle 9
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		jvmTarget = "21"
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

	// FORCE le processus à être léger pour GitHub
	threads.set(1)
	useClasspathFile.set(true)

	// Évite que PIT ne fasse crash le build s'il ne trouve pas de mutants
	// sur une petite modif (sécurité pour la CI)
	failWhenNoMutations.set(false)

	outputFormats.set(listOf("HTML", "XML"))
	timestampedReports.set(false)
}