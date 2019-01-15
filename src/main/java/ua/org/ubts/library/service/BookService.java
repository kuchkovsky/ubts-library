package ua.org.ubts.library.service;

import ua.org.ubts.library.dto.BookDto;
import ua.org.ubts.library.entity.BookEntity;

import java.security.Principal;
import java.util.List;

public interface BookService {

    BookEntity getBook(Long id);

    BookEntity getBook(Long id, Principal principal);

    List<BookEntity> getBooks(Principal principal);

    void createBook(BookDto bookDto);

    void editBook(BookDto bookDto);

    void deleteBook(Long id);

}
