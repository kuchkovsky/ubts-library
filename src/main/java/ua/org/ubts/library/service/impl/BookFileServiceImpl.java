package ua.org.ubts.library.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import ua.org.ubts.library.entity.BookEntity;
import ua.org.ubts.library.entity.FileExtensionEntity;
import ua.org.ubts.library.exception.FileDeleteException;
import ua.org.ubts.library.exception.FileReadException;
import ua.org.ubts.library.exception.FileWriteException;
import ua.org.ubts.library.repository.BookRepository;
import ua.org.ubts.library.repository.FileExtensionRepository;
import ua.org.ubts.library.service.BookFileService;
import ua.org.ubts.library.service.BookService;

import javax.transaction.Transactional;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Transactional
@Slf4j
public class BookFileServiceImpl implements BookFileService {

    private static final String BOOKS_DIRECTORY = "books";

    private static final String COVER_FILENAME = "cover";

    private static final String WRITE_FILE_ERROR_MESSAGE = "Could not save file on server";

    private static final String READ_FILE_ERROR_MESSAGE = "Could not read requested file";

    private static final String BOOK_COVER_DELETE_ERROR_MESSAGE = "Could not delete cover for book with id=";

    @Autowired
    private String appDirectory;

    @Autowired
    private FileExtensionRepository fileExtensionRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    private String getBookDirectory(Long id) {
        return appDirectory + File.separator + BOOKS_DIRECTORY + File.separator + id;
    }

    private Path getPath(Long id, String fileName, String extension) {
        return Paths.get(getBookDirectory(id) + File.separator + fileName + extension);
    }

    private Path getPath(Long id, String fileNameWithExtension) {
        return Paths.get(getBookDirectory(id) + File.separator + fileNameWithExtension);
    }

    private String getMimeTypeFromBytes(byte[] bytes) {
        InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
        try {
            String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
            inputStream.close();
            return mimeType;
        } catch (IOException e) {
            throw new RuntimeException("Failed to decode MimeType from bytes");
        }
    }

    private String getMimeTypeFromPath(Path path) {
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            log.error(READ_FILE_ERROR_MESSAGE, e);
            throw new FileReadException(READ_FILE_ERROR_MESSAGE);
        }
    }

    private String getFileExtension(String mimeType) {
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        try {
            MimeType type = allTypes.forName(mimeType);
            return type.getExtension();
        } catch (MimeTypeException e) {
            throw new RuntimeException("Failed to get extension from mimeType");
        }
    }

    private FileExtensionEntity getFileExtensionEntity(String fileExtension, String mimeType) {
        return fileExtensionRepository.findByName(fileExtension)
                .orElseGet(() -> fileExtensionRepository.saveAndFlush(new FileExtensionEntity(fileExtension, mimeType)));
    }

    @Override
    public void saveCover(Long bookId, String coverFileBase64) {
        byte[] cover = Base64.getDecoder().decode(coverFileBase64);
        try {
            Files.createDirectories(Paths.get(getBookDirectory(bookId)));
            String mimeType = getMimeTypeFromBytes(cover);
            String extension = getFileExtension(mimeType);
            Files.write(getPath(bookId, COVER_FILENAME, extension), cover);
            BookEntity bookEntity = bookService.getBook(bookId);
            FileExtensionEntity fileExtensionEntity = getFileExtensionEntity(extension, mimeType);
            bookEntity.setCoverExtension(fileExtensionEntity);
            bookRepository.save(bookEntity);
        } catch (IOException e) {
            log.error(WRITE_FILE_ERROR_MESSAGE, e);
            throw new FileWriteException(WRITE_FILE_ERROR_MESSAGE);
        }
    }

    @Override
    public String getCoverBase64(BookEntity bookEntity) {
        try {
            String extension = bookEntity.getCoverExtension().getName();
            byte[] bytes = Files.readAllBytes(getPath(bookEntity.getId(), COVER_FILENAME, extension));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            log.error(READ_FILE_ERROR_MESSAGE, e);
            throw new FileReadException(READ_FILE_ERROR_MESSAGE);
        }
    }

    @Override
    public String getCoverDataUrl(BookEntity bookEntity) {
        return "data:" + bookEntity.getCoverExtension().getMimeType() + ";base64," + getCoverBase64(bookEntity);
    }

    @Override
    public String getCoverFilename() {
        return COVER_FILENAME;
    }

    @Override
    public ResponseEntity<Resource> getCover(Long bookId, String coverName) {
        Path filePath = getPath(bookId, coverName);
        return buildResponseEntity(coverName, getMimeTypeFromPath(filePath), filePath);
    }

    @Override
    public void deleteCover(Long bookId) {
        BookEntity bookEntity = bookService.getBook(bookId);
        FileExtensionEntity fileExtensionEntity = bookEntity.getCoverExtension();
        if (fileExtensionEntity != null) {
            try {
                Files.delete(getPath(bookId, COVER_FILENAME, fileExtensionEntity.getName()));
            } catch (IOException e) {
                log.error(BOOK_COVER_DELETE_ERROR_MESSAGE + bookId, e);
                throw new FileDeleteException(BOOK_COVER_DELETE_ERROR_MESSAGE + bookId);
            }
            bookEntity.setCoverExtension(null);
            bookRepository.save(bookEntity);
        }
    }

    private ResponseEntity<Resource> buildResponseEntity(String fileName, String mimeType, Path path) {
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment;filename=" + UriUtils.encodePath(
                                    fileName.replace(",", "_"),"UTF-8")
                    )
                    .contentType(MediaType.parseMediaType(mimeType)).contentLength(Files.size(path))
                    .body(new FileSystemResource(path));
        } catch (IOException e) {
            log.error(READ_FILE_ERROR_MESSAGE, e);
            throw new FileReadException(READ_FILE_ERROR_MESSAGE);
        }
    }

}
