package ua.org.ubts.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenDto extends BaseDto {

    private String token;

}
