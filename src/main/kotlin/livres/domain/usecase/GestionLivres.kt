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
    fun reserverLivre(titre: String) {
        // 1. On cherche le livre
        val livre = repository.trouverParTitre(titre)
            ?: throw NoSuchElementException("Livre non trouvé")

        // 2. On vérifie la règle métier
        if (livre.estReserve) {
            throw IllegalArgumentException("Le livre est déjà réservé")
        }

        // 3. On modifie et on persiste
        livre.estReserve = true
        repository.sauvegarder(livre)
    }

}