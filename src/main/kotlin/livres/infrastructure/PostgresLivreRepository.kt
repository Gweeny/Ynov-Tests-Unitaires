package livres.infrastructure

import livres.domain.model.Livres
import livres.domain.port.LivreRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class PostgresLivreRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) : LivreRepository {

    override fun recupererTout(): List<Livres> {
        // On ajoute est_reserve à la sélection
        val sql = "SELECT titre, auteur, est_reserve FROM livres"
        return jdbcTemplate.query(sql, MapSqlParameterSource()) { rs, _ ->
            Livres(
                titre = rs.getString("titre"),
                auteur = rs.getString("auteur"),
                estReserve = rs.getBoolean("est_reserve")
            )
        }
    }

    override fun sauvegarder(livre: Livres) {
        // On utilise ON CONFLICT pour gérer l'UPDATE si le livre existe déjà (utile pour la réservation)
        val sql = """
            INSERT INTO livres (titre, auteur, est_reserve) 
            VALUES (:titre, :auteur, :estReserve)
            ON CONFLICT (titre) DO UPDATE SET est_reserve = EXCLUDED.est_reserve
        """.trimIndent()

        val params = mapOf(
            "titre" to livre.titre,
            "auteur" to livre.auteur,
            "estReserve" to livre.estReserve
        )
        jdbcTemplate.update(sql, params)
    }

    // AJOUT DE LA MÉTHODE MANQUANTE
    override fun trouverParTitre(titre: String): Livres? {
        val sql = "SELECT titre, auteur, est_reserve FROM livres WHERE titre = :titre"
        val params = mapOf("titre" to titre)
        return try {
            jdbcTemplate.queryForObject(sql, params) { rs, _ ->
                Livres(
                    titre = rs.getString("titre"),
                    auteur = rs.getString("auteur"),
                    estReserve = rs.getBoolean("est_reserve")
                )
            }
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }
}