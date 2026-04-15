package livres.driving.dto

import livres.domain.model.Livres

data class BookDTO(val titre: String, val auteur: String) {
    fun toDomain() = Livres(titre, auteur)

    companion object {
        fun fromDomain(l: Livres) = BookDTO(l.titre, l.auteur)
    }
}