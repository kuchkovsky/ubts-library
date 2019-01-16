package ua.org.ubts.library.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ua.org.ubts.library.dto.UploadedFileDto;
import ua.org.ubts.library.entity.BookEntity;

import javax.servlet.http.HttpServletRequest;

public interface BookFileService {

    void saveCover(Long bookId, String coverFileBase64);

    String getCoverBase64(BookEntity bookEntity);

    String getCoverDataUrl(BookEntity bookEntity);

    String getCoverFilename();

    String getMinimizedCoverFilename();

    String getDocumentFilename(BookEntity bookEntity);

    ResponseEntity<Resource> getCover(Long bookId, String coverName);

    void deleteCover(Long bookId);

    UploadedFileDto saveTemporaryDocument(MultipartFile document);

    void deleteTemporaryDocument(HttpServletRequest request);

    void saveDocument(Long bookId, String uploadedDocument);

    void deleteDocument(Long bookId);

    ResponseEntity<Resource> getDocument(Long bookId);

}
