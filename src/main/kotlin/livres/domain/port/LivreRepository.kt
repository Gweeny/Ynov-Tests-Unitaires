package livres.domain.port

import livres.domain.model.Livres

interface LivreRepository {
    fun sauvegarder(livre: Livres)

    fun recupererTout(): List<Livres>

    fun trouverParTitre(titre: String): Livres?

}