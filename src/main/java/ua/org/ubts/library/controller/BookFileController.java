package ua.org.ubts.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.org.ubts.library.dto.UploadedFileDto;
import ua.org.ubts.library.service.BookFileService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/files/books")
public class BookFileController {

    @Autowired
    private BookFileService bookFileService;

    @GetMapping("/{id}/covers/{fileName}")
    public ResponseEntity<Resource> downloadCover(@PathVariable("id") Long id,
                                                  @PathVariable("fileName") String fileName) {
        return bookFileService.getCover(id, fileName);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/documents")
    public UploadedFileDto uploadTemporaryDocument(@RequestParam("filepond") MultipartFile document) {
        return bookFileService.saveTemporaryDocument(document);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/documents")
    public void deleteTemporaryDocument(HttpServletRequest request) {
        bookFileService.deleteTemporaryDocument(request);
    }

    @GetMapping("/{id}/document")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("id") Long id) {
        return bookFileService.getDocument(id);
    }

}
