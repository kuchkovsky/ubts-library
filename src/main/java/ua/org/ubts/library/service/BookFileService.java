package ua.org.ubts.library.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import ua.org.ubts.library.entity.BookEntity;

public interface BookFileService {

    void saveCover(Long bookId, String coverFileBase64);

    String getCoverBase64(BookEntity bookEntity);

    String getCoverDataUrl(BookEntity bookEntity);

    String getCoverFilename();

    ResponseEntity<Resource> getCover(Long bookId, String coverName);

    void deleteCover(Long bookId);

}
