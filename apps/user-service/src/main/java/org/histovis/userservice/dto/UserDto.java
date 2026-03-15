package org.histovis.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private String username;
    private String firstName;
    private String lastName;
    private boolean isAdmin;
    private String token;
}
