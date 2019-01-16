package ua.org.ubts.library.converter.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.org.ubts.library.converter.BookListItemConverter;
import ua.org.ubts.library.dto.BookListItemDto;
import ua.org.ubts.library.entity.BookEntity;
import ua.org.ubts.library.entity.FileExtensionEntity;
import ua.org.ubts.library.service.BookFileService;

@Component
public class BookListItemConverterImpl implements BookListItemConverter {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BookFileService bookFileService;

    @Override
    public BookEntity convertToEntity(BookListItemDto dto) {
        return modelMapper.map(dto, BookEntity.class);
    }

    @Override
    public BookListItemDto convertToDto(BookEntity entity) {
        BookListItemDto bookListItemDto = modelMapper.map(entity, BookListItemDto.class);
        FileExtensionEntity coverExtension = entity.getCoverExtension();
        if (coverExtension != null) {
            bookListItemDto.setCover(bookFileService.getMinimizedCoverFilename() + coverExtension.getName());
        }
        if (entity.getDocumentExtension() != null) {
            bookListItemDto.setAvailableOnline(true);
        }
        return bookListItemDto;
    }

}
