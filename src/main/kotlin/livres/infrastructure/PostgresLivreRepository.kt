package livres.infrastructure

import livres.domain.model.Livres
import livres.domain.port.LivreRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class PostgresLivreRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) : LivreRepository {

    override fun recupererTout(): List<Livres> {
        val sql = "SELECT titre, auteur FROM livres"
        return jdbcTemplate.query(sql, MapSqlParameterSource()) { rs, _ ->
            Livres(
                titre = rs.getString("titre"),
                auteur = rs.getString("auteur")
            )
        }
    }

    override fun sauvegarder(livre: Livres) {
        val sql = "INSERT INTO livres (titre, auteur) VALUES (:titre, :auteur)"
        val params = mapOf(
            "titre" to livre.titre,
            "auteur" to livre.auteur
        )
        jdbcTemplate.update(sql, params)
    }
}