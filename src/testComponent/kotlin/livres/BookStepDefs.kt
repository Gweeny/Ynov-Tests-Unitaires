package livres

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
import io.restassured.path.json.JsonPath

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookStepDefs {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var lastResponse: ValidatableResponse

    // Helper pour construire l'URL avec le port dynamique injecté par Spring
    private fun url(path: String) = "http://localhost:$port$path"

    @Before
    fun setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Given("l'utilisateur crée le livre {string} écrit par {string}")
    fun createBook(titre: String, auteur: String) {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""{"titre": "$titre", "auteur": "$auteur"}""")
            .`when`()
            .post(url("/books")) // Utilisation de l'URL complète
            .then()
            .statusCode(201)
    }

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

        val body = lastResponse.extract().body().asString()

        // On utilise une recherche (find) pour être sûr de trouver le livre même si l'ordre change
        val jsonPath = JsonPath(body)
        val titreTrouve = jsonPath.getString("find { it.titre == '$expectedTitre' }.titre")
        val auteurTrouve = jsonPath.getString("find { it.titre == '$expectedTitre' }.auteur")

        titreTrouve shouldBe expectedTitre
        auteurTrouve shouldBe expectedAuteur
    }
}