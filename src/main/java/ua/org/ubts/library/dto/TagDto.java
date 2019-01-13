package ua.org.ubts.library.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.org.ubts.library.serializer.TagDtoSerializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = TagDtoSerializer.class)
public class TagDto extends BaseDto {

    private String name;

}
