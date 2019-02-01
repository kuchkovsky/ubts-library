package ua.org.ubts.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.org.ubts.library.converter.CommentConverter;
import ua.org.ubts.library.dto.CommentDto;
import ua.org.ubts.library.service.CommentService;

import java.security.Principal;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentConverter commentConverter;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/books/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createComment(@RequestBody CommentDto commentDto,
                              @PathVariable("id") Long bookId,
                              Principal principal) {
        commentService.createComment(commentConverter.convertToEntity(commentDto), bookId, principal);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable("id") Long id, Authentication authentication) {
        commentService.deleteComment(id, authentication);
    }

}
