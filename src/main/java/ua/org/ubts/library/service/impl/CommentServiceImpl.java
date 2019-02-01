package ua.org.ubts.library.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ua.org.ubts.library.entity.BookEntity;
import ua.org.ubts.library.entity.CommentEntity;
import ua.org.ubts.library.entity.UserEntity;
import ua.org.ubts.library.exception.AccessViolationException;
import ua.org.ubts.library.exception.CommentNotFoundException;
import ua.org.ubts.library.repository.CommentRepository;
import ua.org.ubts.library.service.BookService;
import ua.org.ubts.library.service.CommentService;
import ua.org.ubts.library.service.UserService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.security.Principal;
import java.time.LocalDateTime;

import static ua.org.ubts.library.util.AuthUtil.isAdmin;

@Service
@Transactional
@Slf4j
public class CommentServiceImpl implements CommentService {

    private static final String COMMENT_NOT_FOUND_MESSAGE = "Could not find comment with id=";
    private static final String COMMENT_DELETE_ACCESS_VIOLATION_MESSAGE = "Access denied: " +
            "only administrators are allowed to delete comments of other users";
    private static final String SEND_MAIL_ERROR_MESSAGE = "Could not send email";

    @Value("${UBTS_LIBRARY_SERVER_GMAIL_USERNAME}")
    private String serverGmailUsername;

    @Value("${UBTS_LIBRARY_ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${UBTS_LIBRARY_UI_BOOK_VIEW_PATH}")
    private String bookViewPath;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    public JavaMailSender emailSender;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    private void sendEmailToAdmin(UserEntity userEntity, BookEntity bookEntity, String comment) {
        taskExecutor.execute(() -> {
            try {
                MimeMessage mimeMessage = emailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
                String htmlMessage = "<b>" + userEntity.getLastName() + " " + userEntity.getFirstName() + "</b>"
                        + " залишив новий коментар:"
                        + "<br><br><span style=\"white-space: pre-line\">" + comment + "</span><br><br>"
                        + "<a href=\"" + bookViewPath + "/" + bookEntity.getId() + "\">Переглянути коментар</a>";
                mimeMessage.setContent(htmlMessage, "text/html; charset=UTF-8");
                helper.setTo(adminEmail);
                helper.setSubject("Новий коментар до книги \"" + bookEntity.getTitle() + "\"");
                helper.setFrom(serverGmailUsername + "@gmail.com");
                emailSender.send(mimeMessage);
            } catch (MessagingException e) {
                log.error(SEND_MAIL_ERROR_MESSAGE, e);
            }
        });
    }

    @Override
    public void createComment(CommentEntity commentEntity, Long bookId, Principal principal) {
        BookEntity bookEntity = bookService.getBook(bookId);
        UserEntity userEntity = userService.getUser(principal);
        commentEntity.setBook(bookEntity);
        commentEntity.setUser(userEntity);
        commentEntity.setDateTime(LocalDateTime.now());
        commentRepository.save(commentEntity);
        sendEmailToAdmin(userEntity, bookEntity, commentEntity.getText());
    }

    @Override
    public void deleteComment(Long id, Authentication authentication) {
        CommentEntity commentEntity = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE + id));
        UserEntity currentUser = userService.getUser(authentication);
        if (!commentEntity.getUser().getId().equals(currentUser.getId()) && !isAdmin(authentication)) {
            throw new AccessViolationException(COMMENT_DELETE_ACCESS_VIOLATION_MESSAGE);
        }
        commentRepository.deleteById(id);
    }

}
