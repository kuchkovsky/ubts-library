package ua.org.ubts.library.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import ua.org.ubts.library.dto.UploadedFileDto;
import ua.org.ubts.library.entity.BookEntity;
import ua.org.ubts.library.entity.FileExtensionEntity;
import ua.org.ubts.library.exception.FileDeleteException;
import ua.org.ubts.library.exception.FileReadException;
import ua.org.ubts.library.exception.FileWriteException;
import ua.org.ubts.library.exception.ServiceException;
import ua.org.ubts.library.repository.BookRepository;
import ua.org.ubts.library.repository.FileExtensionRepository;
import ua.org.ubts.library.service.BookFileService;
import ua.org.ubts.library.service.BookService;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class BookFileServiceImpl implements BookFileService {

    private static final String BOOKS_DIRECTORY = "books";
    private static final String COVER_FILENAME = "cover";
    private static final String MINIMIZED_COVER_FILENAME = "cover_min";
    private static final String DOCUMENT_FILENAME = "document";

    private static final int COVER_WIDTH = 600;
    private static final int MINIMIZED_COVER_WIDTH = 300;

    private static final String WRITE_FILE_ERROR_MESSAGE = "Could not save file on server";
    private static final String READ_FILE_ERROR_MESSAGE = "Could not read requested file";
    private static final String BOOK_COVER_DELETE_ERROR_MESSAGE = "Could not delete cover for book with id=";
    private static final String TEMPORARY_DOCUMENT_DELETE_ERROR_MESSAGE = "Could not delete temporary document ";
    private static final String DELETE_REQUEST_DESERIALIZATION_ERROR = "Could not deserialize delete request";
    private static final String DOCUMENT_SAVE_ERROR =  "Could not save document for book with id=";
    private static final String DOCUMENT_DELETE_ERROR =  "Could not delete document for book with id=";

    @Autowired
    private String appDirectory;

    @Autowired
    private String tmpDirectory;

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

    private Path getPath(String temporaryFileName) {
        return Paths.get(tmpDirectory + File.separator + temporaryFileName);
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

    private String getFileExtensionFromMimeType(String mimeType) {
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        try {
            MimeType type = allTypes.forName(mimeType);
            return type.getExtension();
        } catch (MimeTypeException e) {
            throw new RuntimeException("Failed to get extension from mimeType");
        }
    }

    private String getFileExtensionFromName(String fileName) {
       return "." + FilenameUtils.getExtension(fileName);
    }

    private FileExtensionEntity getFileExtensionEntity(String fileExtension, String mimeType) {
        return fileExtensionRepository.findByName(fileExtension)
                .orElseGet(() -> fileExtensionRepository.saveAndFlush(new FileExtensionEntity(fileExtension, mimeType)));
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
            throw new FileReadException(READ_FILE_ERROR_MESSAGE);
        }
    }

    private byte[] resizeImageToWidth(byte[] imageBytes, int width) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        BufferedImage bufferedImage = ImageIO.read(bis);
        bis.close();
        int resizedImageHeight = (int) (bufferedImage.getHeight() / ((double) bufferedImage.getWidth() / width));
        Image resizedImage = bufferedImage.getScaledInstance(width, resizedImageHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedBufferedImage = new BufferedImage(width, resizedImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedBufferedImage.createGraphics();
        g2d.drawImage(resizedImage, 0, 0, null);
        g2d.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String imageType = getFileExtensionFromMimeType(getMimeTypeFromBytes(imageBytes)).substring(1);
        ImageIO.write(resizedBufferedImage, imageType, baos);
        baos.flush();
        byte[] resizedImageBytes = baos.toByteArray();
        baos.close();
        return resizedImageBytes;
    }

    @Override
    public void saveCover(Long bookId, String dataUrl) {
        byte[] cover = Base64.getDecoder().decode(dataUrl.replaceAll("^data:(.*;base64,)?", ""));
        try {
            Files.createDirectories(Paths.get(getBookDirectory(bookId)));
            String mimeType = getMimeTypeFromBytes(cover);
            String extension = getFileExtensionFromMimeType(mimeType);
            byte[] resizedCover = resizeImageToWidth(cover, COVER_WIDTH);
            byte[] minimizedResizedCover = resizeImageToWidth(cover, MINIMIZED_COVER_WIDTH);
            Files.write(getPath(bookId, COVER_FILENAME, extension), resizedCover);
            Files.write(getPath(bookId, MINIMIZED_COVER_FILENAME, extension), minimizedResizedCover);
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
            throw new FileReadException(READ_FILE_ERROR_MESSAGE);
        }
    }

    @Override
    public String getCoverDataUrl(BookEntity bookEntity) {
        if (bookEntity.getCoverExtension() != null) {
            return "data:" + bookEntity.getCoverExtension().getMimeType() + ";base64," + getCoverBase64(bookEntity);
        }
        return null;
    }

    @Override
    public String getCoverFilename() {
        return COVER_FILENAME;
    }

    @Override
    public String getMinimizedCoverFilename() {
        return MINIMIZED_COVER_FILENAME;
    }

    @Override
    public String getDocumentFilename(BookEntity bookEntity) {
        FileExtensionEntity documentExtension = bookEntity.getDocumentExtension();
        if (documentExtension != null) {
            return bookEntity.getTitle() + " - " + bookEntity.getPublisher() + documentExtension.getName();
        }
        return null;
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
                Files.delete(getPath(bookId, MINIMIZED_COVER_FILENAME, fileExtensionEntity.getName()));
            } catch (IOException e) {
                log.error(BOOK_COVER_DELETE_ERROR_MESSAGE + bookId, e);
                throw new FileDeleteException(BOOK_COVER_DELETE_ERROR_MESSAGE + bookId);
            }
            bookEntity.setCoverExtension(null);
            bookRepository.save(bookEntity);
        }
    }

    @Override
    public UploadedFileDto saveTemporaryDocument(MultipartFile document) {
        String fileName = UUID.randomUUID().toString() + getFileExtensionFromName(document.getOriginalFilename());
        String path = tmpDirectory + File.separator + fileName;
        try {
            document.transferTo(Paths.get(path));
        } catch (IOException e) {
            log.error(WRITE_FILE_ERROR_MESSAGE, e);
            throw new FileWriteException(WRITE_FILE_ERROR_MESSAGE);
        }
        return new UploadedFileDto(fileName);
    }

    @Override
    public void deleteTemporaryDocument(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        UploadedFileDto document;
        try {
            document = mapper.readValue(request.getInputStream(), UploadedFileDto.class);
        } catch (IOException e) {
            log.error(DELETE_REQUEST_DESERIALIZATION_ERROR);
            throw new ServiceException(DELETE_REQUEST_DESERIALIZATION_ERROR);
        }
        String fileName = document.getFileName();
        try {
            Files.delete(getPath(fileName));
        } catch (IOException e) {
            log.error(TEMPORARY_DOCUMENT_DELETE_ERROR_MESSAGE + fileName, e);
            throw new FileDeleteException(TEMPORARY_DOCUMENT_DELETE_ERROR_MESSAGE + fileName);
        }
    }

    @Override
    public void saveDocument(Long bookId, String uploadedDocument) {
        BookEntity bookEntity = bookService.getBook(bookId);
        String extension = getFileExtensionFromName(uploadedDocument);
        Path pathFrom = getPath(uploadedDocument);
        Path pathTo = getPath(bookId, DOCUMENT_FILENAME, extension);
        try {
            Files.createDirectories(Paths.get(getBookDirectory(bookId)));
            Files.move(pathFrom, pathTo);
        } catch (IOException e) {
            log.error(DOCUMENT_SAVE_ERROR + bookId, e);
            throw new FileDeleteException(DOCUMENT_SAVE_ERROR + bookId);
        }
        bookEntity.setDocumentExtension(getFileExtensionEntity(extension, getMimeTypeFromPath(pathTo)));
        bookRepository.save(bookEntity);
    }

    @Override
    public void deleteDocument(Long bookId) {
        BookEntity bookEntity = bookService.getBook(bookId);
        Path path = getPath(bookId, DOCUMENT_FILENAME, bookEntity.getDocumentExtension().getName());
        try {
            Files.delete(path);
        } catch (IOException e) {
            log.error(DOCUMENT_DELETE_ERROR + bookId, e);
            throw new FileDeleteException(DOCUMENT_DELETE_ERROR + bookId);
        }
        bookEntity.setDocumentExtension(null);
        bookRepository.save(bookEntity);
    }

    @Override
    public ResponseEntity<Resource> getDocument(Long bookId) {
        BookEntity bookEntity = bookService.getBook(bookId);
        String fileName = getDocumentFilename(bookEntity);
        String mimeType = bookEntity.getDocumentExtension().getMimeType();
        String extension = bookEntity.getDocumentExtension().getName();
        Path document = getPath(bookId, DOCUMENT_FILENAME, extension);
        return buildResponseEntity(fileName, mimeType, document);
    }

}
