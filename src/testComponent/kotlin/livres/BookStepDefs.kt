package livres

import com.example.demo.DemoApplication
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.ValidatableResponse
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.http.HttpStatus

@CucumberContextConfiguration
@SpringBootTest(
    classes = [DemoApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class BookStepDefs {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var lastResponse: ValidatableResponse

    private fun url(path: String) = "http://localhost:$port$path"

    @Before
    fun setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    // --- STEPS DE CRÉATION ---

    @Given("l'utilisateur crée le livre {string} écrit par {string}")
    fun createBook(titre: String, auteur: String) {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""{"titre": "$titre", "auteur": "$auteur", "estReserve": false}""")
            .`when`()
            .post(url("/books"))
            .then()
            .statusCode(201)
    }

    // --- STEPS DE RÉCUPÉRATION ---

    @When("l'utilisateur récupère la liste des livres")
    fun getAllBooks() {
        lastResponse = RestAssured.given()
            .`when`()
            .get(url("/books"))
            .then()
            .statusCode(200)
    }

    @Then("la liste doit contenir le livre suivant")
    fun shouldContainBook(payload: List<Map<String, String>>) {
        val expectedTitre = payload[0]["titre"]
        val expectedAuteur = payload[0]["auteur"]

        val books: List<Map<String, Any>> = lastResponse.extract().body().jsonPath().getList("")
        val livreTrouve = books.find { it["titre"] == expectedTitre }

        livreTrouve shouldNotBe null
        livreTrouve!!["titre"] shouldBe expectedTitre
        livreTrouve["auteur"] shouldBe expectedAuteur
    }

    // --- STEPS DE RÉSERVATION (LA NOUVELLE FEATURE) ---

    @Given("que le livre {string} écrit par {string} existe")
    fun leLivreExiste(titre: String, auteur: String) {
        // On s'assure que le livre est là avant de tester le PATCH
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""{"titre": "$titre", "auteur": "$auteur", "estReserve": false}""")
            .`when`()
            .post(url("/books"))
            .then()
            .statusCode(201)
    }

    @When("l'utilisateur réserve le livre {string}")
    fun reserverLivre(titre: String) {
        lastResponse = RestAssured.given()
            .`when`()
            .patch(url("/books/$titre/reserve"))
            .then()
    }

    @Then("la réservation est confirmée")
    fun confirmationReservation() {
        // On accepte 200 ou 204 selon ton Controller
        val status = lastResponse.extract().statusCode()
        (status == 200 || status == 204) shouldBe true
    }

    @Then("le livre {string} doit avoir le statut réservé à {string}")
    fun verifierStatut(titre: String, statutAttendu: String) {
        val expectedStatus = statutAttendu.toBoolean()

        RestAssured.given()
            .`when`() // <--- Vérifie bien les deux ` ici
            .get(url("/books"))
            .then()
            .statusCode(200)
            .body("find { it.titre == '$titre' }.estReserve", org.hamcrest.Matchers.`is`(expectedStatus))
    }
}