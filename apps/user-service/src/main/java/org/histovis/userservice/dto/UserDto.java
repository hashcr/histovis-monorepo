package org.histovis.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private String username;
    private String firstName;
    private String lastName;
    @JsonProperty("isAdmin")
    private boolean isAdmin;
    private String token;
}
