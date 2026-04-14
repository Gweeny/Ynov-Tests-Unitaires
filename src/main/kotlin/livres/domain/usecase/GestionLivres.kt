package livres.domain.usecase

import livres.domain.model.Livres
import livres.domain.port.LivreRepository

class GestionLivres(private val repository: LivreRepository) {

    fun ajouterLivre(livre: Livres) {
        if (livre.titre.isBlank() || livre.auteur.isBlank()) {
            throw IllegalArgumentException("Le titre et l'auteur ne peuvent pas être vides")
        }
        repository.sauvegarder(livre)
    }
    fun listerLivresTries(): List<Livres> {
        // Grâce au ménage fait dans le Port, plus besoin de "?" ici
        return repository.recupererTout().sortedBy { it.titre.lowercase() }
    }

}