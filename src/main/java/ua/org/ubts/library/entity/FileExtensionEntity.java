package ua.org.ubts.library.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "file_extension")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileExtensionEntity extends BaseEntity<Integer> {

    @NotEmpty
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "mime_type")
    private String mimeType;

}
