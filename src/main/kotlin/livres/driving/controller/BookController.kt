package livres.driving.controller

import livres.domain.usecase.GestionLivres
import livres.driving.dto.BookDTO
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
class BookController(private val gestionLivres: GestionLivres) {

    @GetMapping
    fun getBooks(): List<BookDTO> {
        return gestionLivres.listerLivresTries().map { BookDTO.fromDomain(it) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBook(@RequestBody book: BookDTO) {
        gestionLivres.ajouterLivre(book.toDomain())
    }
}