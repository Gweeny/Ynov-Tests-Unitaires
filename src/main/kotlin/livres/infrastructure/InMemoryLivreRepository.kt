package livres.infrastructure

import livres.domain.model.Livres
import livres.domain.port.LivreRepository
import org.springframework.stereotype.Repository

//@Repository
class InMemoryLivreRepository : LivreRepository {
    private val storage = mutableListOf<Livres>()

    override fun sauvegarder(livre: Livres) {
        // Pour éviter les doublons lors des tests, on peut supprimer l'ancien s'il existe
        storage.removeIf { it.titre == livre.titre }
        storage.add(livre)
    }

    override fun recupererTout(): List<Livres> = storage

    // AJOUT DE LA MÉTHODE MANQUANTE
    override fun trouverParTitre(titre: String): Livres? {
        return storage.find { it.titre == titre }
    }
}