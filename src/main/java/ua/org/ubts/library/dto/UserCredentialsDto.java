package ua.org.ubts.library.dto;

import lombok.Data;

@Data
public class UserCredentialsDto extends BaseDto {

    private String login;
    private String password;

}
