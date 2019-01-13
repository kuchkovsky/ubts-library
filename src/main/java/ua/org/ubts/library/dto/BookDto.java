package ua.org.ubts.library.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookDto extends BaseDto {

    private String id;

    private String title;

    private String author;

    private String publisher;

    private Integer publicationYear;

    private Integer pages;

    private String description;

    private Boolean availableOffline;

    private List<TagDto> tags;

    private String coverFileName;

    private String coverFile;

}
