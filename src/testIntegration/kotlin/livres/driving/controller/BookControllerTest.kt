package livres.driving.controller

import com.example.demo.DemoApplication // <-- AJOUTE CET IMPORT
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.verify
import livres.domain.model.Livres
import livres.domain.usecase.GestionLivres
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

// ON DIT EXPLICITEMENT À SPRING OÙ EST LA CONFIGURATION
@WebMvcTest(controllers = [BookController::class], properties = ["spring.main.allow-bean-definition-overriding=true"])
@org.springframework.test.context.ContextConfiguration(classes = [DemoApplication::class])
class BookControllerTest : DescribeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var gestionLivres: GestionLivres

    init {
        it("GET /books doit retourner la liste en JSON") {
            val livreDomaine = Livres("Le Hobbit", "Tolkien")
            every { gestionLivres.listerLivresTries() } returns listOf(livreDomaine)

            mockMvc.perform(get("/books"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].titre").value("Le Hobbit"))
        }

        it("POST /books doit créer un livre et retourner 201") {
            val json = """ { "titre": "1984", "auteur": "Orwell" } """
            every { gestionLivres.ajouterLivre(any()) } returns Unit

            mockMvc.perform(
                post("/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
                .andExpect(status().isCreated)

            verify { gestionLivres.ajouterLivre(any()) }
        }
    }
}