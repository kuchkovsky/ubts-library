package ua.org.ubts.library.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookListItemDto extends BaseDto {

    private String id;

    private String title;

    private String author;

    private String publisher;

    private Boolean availableOffline;

    private Boolean availableOnline;

    private List<TagDto> tags;

    private String cover;

}
