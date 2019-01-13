package ua.org.ubts.library.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
public class BookEntity extends BaseEntity<Long> {

    @NotEmpty
    @Column(name = "title")
    private String title;

    @NotEmpty
    @Column(name = "author")
    private String author;

    @NotEmpty
    @Column(name = "publisher")
    private String publisher;

    @NotNull
    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @NotNull
    @Column(name = "pages", nullable = false)
    private Integer pages;

    @NotEmpty
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_available_offline")
    private boolean availableOffline;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "cover_ext_id")
    private FileExtensionEntity coverExtension;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "file_ext_id")
    private FileExtensionEntity fileExtension;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "book_tag",
            joinColumns = @JoinColumn(
                    name = "tag_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "book_id", referencedColumnName = "id"))
    private List<TagEntity> tags;

}
