package ua.org.ubts.library.service;

import org.springframework.security.core.Authentication;
import ua.org.ubts.library.entity.CommentEntity;

import java.security.Principal;

public interface CommentService {

    void createComment(CommentEntity commentEntity, Long bookId, Principal principal);

    void deleteComment(Long id, Authentication authentication);

}
