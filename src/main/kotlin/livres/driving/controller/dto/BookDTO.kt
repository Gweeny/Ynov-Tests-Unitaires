package livres.driving.dto

import livres.domain.model.Livres

data class BookDTO(val titre: String, val auteur: String, val estReserve : Boolean) {
    fun toDomain() = Livres(titre, auteur, estReserve)

    companion object {
        fun fromDomain(l: Livres) = BookDTO(l.titre, l.auteur, l.estReserve)
    }
}