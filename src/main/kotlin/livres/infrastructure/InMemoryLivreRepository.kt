package livres.infrastructure

import livres.domain.model.Livres
import livres.domain.port.LivreRepository
import org.springframework.stereotype.Repository

//@Repository
class InMemoryLivreRepository : LivreRepository {
    private val storage = mutableListOf<Livres>()

    override fun sauvegarder(livre: Livres) {
        storage.add(livre)
    }

    // VERIFIE BIEN LE NOM ICI (sans 's' si l'erreur le dit)
    override fun recupererTout(): List<Livres> = storage
}