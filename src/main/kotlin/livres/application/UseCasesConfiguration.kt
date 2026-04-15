package livres.application

import livres.domain.port.LivreRepository
import livres.domain.usecase.GestionLivres
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration // Indique à Spring que ce fichier contient des définitions de Beans
class UseCasesConfiguration {

    @Bean // Spring appelle cette fonction au démarrage
    fun gestionLivres(livreRepository: LivreRepository): GestionLivres {
        // Spring voit que GestionLivres a besoin d'un LivreRepository.
        // Il va chercher s'il existe un Bean de ce type (notre InMemoryLivreRepository)
        // et l'injecter ici automatiquement.
        return GestionLivres(livreRepository)
    }
}