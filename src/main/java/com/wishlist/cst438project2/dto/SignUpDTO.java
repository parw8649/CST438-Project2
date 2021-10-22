package com.wishlist.cst438project2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDTO {

    @NotEmpty(message = "Firstname must not be null or empty")
    private String firstName;

    @NotEmpty(message = "lastname must not be null or empty")
    private String lastName;

    @NotEmpty(message = "EmailId must not be null or empty")
    @Pattern(regexp = "^[A-Za-z0-9+.!#$%&'*+-/?^_`{ |-]+@[A-Za-z0-9.-]+$", message = "Invalid email id format.")
    private String emailId;

    @NotEmpty(message = "Username must not be null or empty")
    private String username;

    @Pattern(regexp = "^[a-zA-Z0-9!@&_?$#]+$", message = "Invalid Password format.")
    @Size(min = 6, message = "Password size must be minimum 6")
    private String password;
}
