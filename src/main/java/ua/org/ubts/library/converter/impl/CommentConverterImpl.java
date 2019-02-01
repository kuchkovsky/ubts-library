package ua.org.ubts.library.converter.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.org.ubts.library.converter.CommentConverter;
import ua.org.ubts.library.dto.CommentDto;
import ua.org.ubts.library.entity.CommentEntity;

@Component
public class CommentConverterImpl implements CommentConverter {

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CommentEntity convertToEntity(CommentDto dto) {
        return modelMapper.map(dto, CommentEntity.class);
    }

    @Override
    public CommentDto convertToDto(CommentEntity entity) {
        return modelMapper.map(entity, CommentDto.class);
    }

}
