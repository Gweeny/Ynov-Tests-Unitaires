package livres.infrastructure

import com.example.demo.DemoApplication  // <--- VÉRIFIE BIEN CET IMPORT
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import livres.domain.model.Livres
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [DemoApplication::class]) // <--- ON LUI DIT OÙ CHERCHER ICI
@ActiveProfiles("testIntegration")
class PostgresLivreRepositoryIT : FunSpec() {

    @Autowired
    private lateinit var repository: PostgresLivreRepository

    init {
        extension(SpringExtension)

        test("sauvegarder doit réellement insérer un livre en BDD") {
            val nouveauLivre = Livres("Le Chuchoteur", "Donato Carrisi")
            repository.sauvegarder(nouveauLivre)
            val tousLesLivres = repository.recupererTout()
            tousLesLivres.any { it.titre == "Le Chuchoteur" } shouldBe true
        }
    }

    companion object {
        init {
            System.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/postgres")
            System.setProperty("spring.datasource.username", "postgres")
            System.setProperty("spring.datasource.password", "password")
            System.setProperty("spring.liquibase.enabled", "true")
        }
    }
}