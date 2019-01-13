package ua.org.ubts.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.org.ubts.library.service.BookFileService;

@RestController
@RequestMapping("/api/files/books")
public class BookFileApiController {

    @Autowired
    private BookFileService bookFileService;

    @GetMapping("/{id}/covers/{fileName}")
    public ResponseEntity<Resource> getTrackPdfChords(@PathVariable("id") Long id,
                                                      @PathVariable("fileName") String fileName) {
        return bookFileService.getCover(id, fileName);
    }

}
