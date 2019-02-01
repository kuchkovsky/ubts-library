package ua.org.ubts.library.service.impl;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ua.org.ubts.library.converter.BookConverter;
import ua.org.ubts.library.dto.BookDto;
import ua.org.ubts.library.entity.BookEntity;
import ua.org.ubts.library.entity.TagEntity;
import ua.org.ubts.library.exception.BookNotFoundException;
import ua.org.ubts.library.repository.BookRepository;
import ua.org.ubts.library.repository.TagRepository;
import ua.org.ubts.library.service.BookFileService;
import ua.org.ubts.library.service.BookService;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ua.org.ubts.library.util.AuthUtil.isAdmin;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private static final String BOOK_NOT_FOUND_MESSAGE = "Could not find book with id=";

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private BookFileService bookFileService;

    @Autowired
    private BookConverter bookConverter;

    private static Predicate<BookEntity> checkPermission(Principal principal) {
        return bookEntity -> principal != null || bookEntity.isAvailableOffline();
    }

    private static Predicate<BookEntity> checkPermission(Authentication authentication) {
        return bookEntity -> authentication != null || bookEntity.isAvailableOffline();
    }

    private static Supplier<BookNotFoundException> supplyBookNotFoundException(Long id) {
        return () -> new BookNotFoundException(BOOK_NOT_FOUND_MESSAGE + id);
    }

    private void setTagsFromDb(BookEntity bookEntity) {
        if (bookEntity.getTags() != null) {
            List<TagEntity> tags = bookEntity.getTags().stream()
                    .map(tagEntity -> tagRepository.findByName(tagEntity.getName())
                            .orElse(tagEntity))
                    .collect(Collectors.toList());
            bookEntity.setTags(tags);
            tagRepository.saveAll(tags);
        }
    }

    @Override
    public BookEntity getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(supplyBookNotFoundException(id));
    }

    @Override
    public BookEntity getBook(Long id, Authentication authentication) {
        return bookRepository.findById(id)
                .filter(checkPermission(authentication))
                .map(bookEntity -> {
                    Hibernate.initialize(bookEntity.getComments());
                    BookEntity book = SerializationUtils.clone(bookEntity);
                    if (authentication == null) {
                        book.setDocumentExtension(null);
                    }
                    if (authentication == null || !isAdmin(authentication)) {
                        book.setClassifier(null);
                        book.setNotes(null);
                    }
                    Collections.reverse(book.getComments());
                    return book;
                })
                .orElseThrow(supplyBookNotFoundException(id));
    }

    @Override
    public List<BookEntity> getBooks(Principal principal) {
        List<BookEntity> bookList = bookRepository.findAll().stream()
                .filter(checkPermission(principal))
                .map(bookEntity -> {
                    BookEntity book = SerializationUtils.clone(bookEntity);
                    if (principal == null) {
                        book.setDocumentExtension(null);
                    }
                    return book;
                })
                .collect(Collectors.toList());
        Collections.reverse(bookList);
        return bookList;
    }

    @Override
    public void createBook(BookDto bookDto) {
        BookEntity bookEntity = bookConverter.convertToEntity(bookDto);
        setTagsFromDb(bookEntity);
        bookEntity.setId(null);
        Long bookId = bookRepository.saveAndFlush(bookEntity).getId();
        String coverFileDataUrl = bookDto.getCoverFile();
        if (StringUtils.isNotEmpty(coverFileDataUrl)) {
            bookFileService.saveCover(bookId, coverFileDataUrl);
        }
        String uploadedDocument = bookDto.getUploadedDocument();
        if (StringUtils.isNotEmpty(uploadedDocument)) {
            bookFileService.saveDocument(bookId, uploadedDocument);
        }
    }

    @Override
    public void editBook(BookDto bookDto) {
        BookEntity bookEntity = bookConverter.convertToEntity(bookDto);
        BookEntity bookEntityFromDb = getBook(bookEntity.getId());
        setTagsFromDb(bookEntity);
        bookEntity.setCoverExtension(bookEntityFromDb.getCoverExtension());
        bookEntity.setDocumentExtension(bookEntityFromDb.getDocumentExtension());
        bookRepository.save(bookEntity);
        String coverFileDataUrl = bookDto.getCoverFile();
        if (StringUtils.isNotEmpty(coverFileDataUrl)) {
            bookFileService.saveCover(bookEntity.getId(), coverFileDataUrl);
        } else {
            bookFileService.deleteCover(bookEntity.getId());
        }
        String uploadedDocument = bookDto.getUploadedDocument();
        if (StringUtils.isNotEmpty(uploadedDocument)) {
            bookFileService.saveDocument(bookEntity.getId(), uploadedDocument);
            return;
        }
        String document = bookDto.getDocument();
        if (StringUtils.isEmpty(document) && bookEntityFromDb.getDocumentExtension() != null) {
            bookFileService.deleteDocument(bookEntity.getId());
        }
    }

    @Override
    public void deleteBook(Long id) {
        BookEntity bookEntity = bookRepository.findById(id)
                .orElseThrow(supplyBookNotFoundException(id));
        if (bookEntity.getCoverExtension() != null) {
            bookFileService.deleteCover(id);
        }
        if (bookEntity.getDocumentExtension() != null) {
            bookFileService.deleteDocument(id);
        }
        bookRepository.deleteById(id);
    }

}
