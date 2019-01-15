package ua.org.ubts.library.converter.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.org.ubts.library.converter.BookConverter;
import ua.org.ubts.library.dto.BookDto;
import ua.org.ubts.library.entity.BookEntity;
import ua.org.ubts.library.service.BookFileService;

@Component
public class BookConverterImpl implements BookConverter {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BookFileService bookFileService;

    @Override
    public BookEntity convertToEntity(BookDto dto) {
        return modelMapper.map(dto, BookEntity.class);
    }

    @Override
    public BookDto convertToDto(BookEntity entity) {
        BookDto bookDto = modelMapper.map(entity, BookDto.class);
        bookDto.setCoverFile(bookFileService.getCoverDataUrl(entity));
        bookDto.setDocument(bookFileService.getDocumentFilename(entity));
        return bookDto;
    }

}
