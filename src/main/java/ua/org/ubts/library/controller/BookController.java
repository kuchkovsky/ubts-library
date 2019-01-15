package ua.org.ubts.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.org.ubts.library.converter.BookConverter;
import ua.org.ubts.library.converter.BookListItemConverter;
import ua.org.ubts.library.dto.BookDto;
import ua.org.ubts.library.dto.BookListItemDto;
import ua.org.ubts.library.service.BookService;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookConverter bookConverter;

    @Autowired
    private BookListItemConverter bookListItemConverter;

    @GetMapping
    public List<BookListItemDto> getBooks(Principal principal) {
        List<BookListItemDto> books = bookListItemConverter.convertToDto(bookService.getBooks(principal));
        return books.stream().peek(book -> {
            if (principal == null) {
                book.setAvailableOnline(null);
            }
        }).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createBook(@RequestBody BookDto bookDto) {
        bookService.createBook(bookDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public void editBook(@RequestBody BookDto bookDto) {
        bookService.editBook(bookDto);
    }

    @GetMapping("/{id}")
    public BookDto getBook(@PathVariable("id") Long id, Principal principal) {
        return bookConverter.convertToDto(bookService.getBook(id, principal));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable("id") Long id) {
        bookService.deleteBook(id);
    }

}
