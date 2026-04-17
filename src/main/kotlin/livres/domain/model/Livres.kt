package livres.domain.model

data class Livres(
    var titre: String,
    var auteur: String,
    var estReserve : Boolean = false,
)