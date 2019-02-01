package ua.org.ubts.library.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
public class CommentEntity extends BaseEntity<Long> {

    @NotEmpty
    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    private BookEntity book;

    @ManyToOne(fetch = FetchType.EAGER)
    private UserEntity user;

    @Column
    private LocalDateTime dateTime;

}
