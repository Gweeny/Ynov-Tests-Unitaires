package livres.domain.usecase

import io.kotest.assertions.throwables.shouldNotThrowMessage
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import livres.domain.model.Livres
import livres.domain.port.LivreRepository

class GestionLivresTest : FunSpec({

    val repository = mockk<LivreRepository>(relaxed = true)
    val useCase = GestionLivres(repository)

    test("Un livre doit être ajouté avec succès si les données sont valides") {
        val livre = Livres("Le Petit Prince", "Saint-Exupéry", estReserve = false)
        useCase.ajouterLivre(livre)
        verify { repository.sauvegarder(livre) }
    }

    test("L'ajout d'un livre doit échouer si le titre est vide") {
        val livreSansTitre = Livres("", "Saint-Exupéry", estReserve = false)
        shouldThrowMessage("Le titre et l'auteur ne peuvent pas être vides") {
            useCase.ajouterLivre(livreSansTitre)
        }
    }

    test("L'ajout d'un livre doit échouer si l'auteur est vide") {
        val livreSansAuteur = Livres("Le Petit Prince", "  ", estReserve = false)
        shouldThrowMessage("Le titre et l'auteur ne peuvent pas être vides") {
            useCase.ajouterLivre(livreSansAuteur)
        }
    }

    test("La liste doit être retournée triée par titre par ordre alphabétique") {
        val livreA = Livres("Antigone", "Anouilh", estReserve = false)
        val livreM = Livres("Miserables", "Hugo", estReserve = false)
        val livreZ = Livres("Zola", "Emile", estReserve = false)

        every { repository.recupererTout() } returns listOf(livreZ, livreA, livreM)

        val resultat = useCase.listerLivresTries()

        resultat[0].titre shouldBe "Antigone"
        resultat[1].titre shouldBe "Miserables"
        resultat[2].titre shouldBe "Zola"
    }

    test("La liste des livres retournés contient tous les éléments de la liste stockée") {
        // Générateur de livres aléatoires
        val arbLivre = Arb.string(minSize = 1, maxSize = 20).map { titre ->
            Livres(titre, "Auteur Aléatoire", estReserve = false)
        }

        checkAll(Arb.list(arbLivre, 1..50)) { listeGeneree ->
            every { repository.recupererTout() } returns listeGeneree

            val resultat = useCase.listerLivresTries()

            resultat.size shouldBe listeGeneree.size
            // Vérifie que les deux listes ont le même contenu, peu importe l'ordre
            resultat shouldContainExactlyInAnyOrder listeGeneree
        }
    }

    test(name = "Le livre existe et le livre n'est pas réservé") {
        val livre = Livres("Antigone", "Anouilh", estReserve = false)
            every { repository.trouverParTitre(livre.titre) } returns livre
        useCase.reserverLivre(titre = "Antigone")
        verify { repository.sauvegarder(match { it.titre == livre.titre && it.estReserve }) }
    }

    test(name = "Le livre existe mais il est déjà réservé"){
        val livre = Livres(titre = "Antigone", "Anouilh", estReserve = true)
        every { repository.trouverParTitre(livre.titre) } returns livre
        shouldThrowMessage("Le livre est déjà réservé"){
            useCase.reserverLivre("Antigone")
        }
    }

    test(name = "Le livre est inexistant"){
        val livre = Livres("UnTitreRandom", "Random", estReserve = false)
        every { repository.trouverParTitre(livre.titre) } returns null
        shouldThrowMessage("Livre non trouvé"){
            useCase.reserverLivre("UnTitreRandom")
        }
    }
})