package ua.org.ubts.library.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private static Supplier<BookNotFoundException> supplyBookNotFoundException(Long id) {
        return () -> new BookNotFoundException(BOOK_NOT_FOUND_MESSAGE + id);
    }

    private void setTagsFromDb(BookEntity bookEntity) {
        if (bookEntity.getTags() !=  null) {
            List<TagEntity> tags = bookEntity.getTags().stream()
                    .map(tagEntity -> tagRepository.findByName(tagEntity.getName())
                            .orElse(tagEntity))
                    .collect(Collectors.toList());
            bookEntity.setTags(tags);
        }
    }

    @Override
    public BookEntity getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(supplyBookNotFoundException(id));
    }

    @Override
    public BookEntity getBook(Long id, Principal principal) {
        return bookRepository.findById(id)
                .filter(checkPermission(principal))
                .orElseThrow(supplyBookNotFoundException(id));
    }

    @Override
    public List<BookEntity> getBooks(Principal principal) {
        return bookRepository.findAll().stream()
                .filter(checkPermission(principal))
                .collect(Collectors.toList());
    }

    @Override
    public void createBook(BookDto bookDto) {
        BookEntity bookEntity = bookConverter.convertToEntity(bookDto);
        setTagsFromDb(bookEntity);
        bookEntity.setId(null);
        Long bookId = bookRepository.saveAndFlush(bookEntity).getId();
        String coverFileBase64 = bookDto.getCoverFile();
        if (StringUtils.isNotEmpty(coverFileBase64)) {
            bookFileService.saveCover(bookId, coverFileBase64);
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
        String coverFileBase64 = bookDto.getCoverFile();
        if (StringUtils.isNotEmpty(coverFileBase64)) {
            bookFileService.saveCover(bookEntity.getId(), coverFileBase64);
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
