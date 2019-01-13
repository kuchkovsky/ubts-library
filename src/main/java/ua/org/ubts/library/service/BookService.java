package ua.org.ubts.library.service;

import ua.org.ubts.library.entity.BookEntity;

import java.security.Principal;
import java.util.List;

public interface BookService {

    BookEntity getBook(Long id);

    BookEntity getBook(Long id, Principal principal);

    List<BookEntity> getBooks(Principal principal);

    void createBook(BookEntity bookEntity, String coverFileBase64);

    void editBook(BookEntity bookEntity, String coverFileBase64);

    void deleteBook(Long id);

}
