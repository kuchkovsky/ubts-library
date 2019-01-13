package ua.org.ubts.library.converter.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.org.ubts.library.converter.UserConverter;
import ua.org.ubts.library.dto.UserDto;
import ua.org.ubts.library.entity.UserEntity;

@Component
public class UserConverterImpl implements UserConverter {

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserEntity convertToEntity(UserDto dto) {
        return modelMapper.map(dto, UserEntity.class);
    }

    @Override
    public UserDto convertToDto(UserEntity entity) {
        return modelMapper.map(entity, UserDto.class);
    }

}
