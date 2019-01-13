package ua.org.ubts.library.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ua.org.ubts.library.dto.TagDto;

import java.io.IOException;

public class TagDtoSerializer extends StdSerializer<TagDto> {

    public TagDtoSerializer() {
        this(null);
    }

    public TagDtoSerializer(Class<TagDto> t) {
        super(t);
    }

    @Override
    public void serialize(TagDto tagDto, JsonGenerator jsonGenerator, SerializerProvider provider)
    throws IOException {
        jsonGenerator.writeString(tagDto.getName());
    }

}
