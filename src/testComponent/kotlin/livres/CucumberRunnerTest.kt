package livres

import com.example.demo.DemoApplication // <-- Assure-toi que l'import vers ta classe Application est correct
import io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME
import org.junit.platform.suite.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.lifecycle.Startables

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features") // On pointe le dossier, pas le fichier seul
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "livres")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [DemoApplication::class])
class CucumberRunnerTest {

    companion object {
        private val container = PostgreSQLContainer<Nothing>("postgres:15-alpine")

        init {
            Startables.deepStart(container).join()
        }

        @JvmStatic
        @DynamicPropertySource
        fun overrideProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.username") { container.username }
            registry.add("spring.datasource.password") { container.password }
            registry.add("spring.datasource.url") { container.jdbcUrl }
            // On désactive liquibase sur les tests de composants si besoin,
            // mais ici on le laisse pour que la table soit créée dans le container
        }
    }
}