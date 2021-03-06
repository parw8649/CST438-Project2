package com.wishlist.cst438project2;

import com.wishlist.cst438project2.common.Constants;
import com.wishlist.cst438project2.common.Utils;
import com.wishlist.cst438project2.controller.UserController;
import com.wishlist.cst438project2.dto.*;
import com.wishlist.cst438project2.integration.FirebaseIntegration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for user functionalities
 * @author Chaitanya Parwatkar
 * @version %I% %G%
 */

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {

    @Autowired
    private UserController userController;

    @Autowired
    FirebaseIntegration firebaseIntegration;

    final String USERNAME = "test-user22";
    final String FIRSTNAME = "test";
    final String LASTNAME = "user22";
    final String EMAIL = "testuser22@gmail.com";
    final String NEW_PASSWORD = "user-pass32";
    String PASSWORD = "user-pass22";

    @Test
    @Order(1)
    void saveUser_Success() {

        UserDTO userDTO = firebaseIntegration.getUser(USERNAME);
        if(Objects.nonNull(userDTO)) {
            firebaseIntegration.deleteUser(USERNAME);
        }

        SignUpDTO signUpDTO = new SignUpDTO(FIRSTNAME, LASTNAME, EMAIL, USERNAME, PASSWORD);
        String responseTimestamp = userController.saveUser(signUpDTO);
        assertThat(responseTimestamp, notNullValue());
    }

    @Test
    @Order(2)
    void userLogin_Success() {
        SignInDTO credentials = new SignInDTO();
        credentials.setUsername(USERNAME);
        credentials.setPassword(PASSWORD);

        ResponseDTO<UserLoginDTO> response = userController.login(credentials);

        String token = response.getData().getAccessToken();

        assertThat(token, notNullValue());
    }

    @Test
    @Order(3)
    void updateUser_Success() {

        String newFirstName = "unitTest";
        String newLastName = "user";
        String newEmail = "unittestuser@gmail.com";

        UserDTO updateUserRequest = new UserDTO(newFirstName, newLastName, newEmail, USERNAME, null);
        UserDTO updateUserResponse = userController.updateUser(getAccessToken(PASSWORD), updateUserRequest);

        assertThat(updateUserResponse, notNullValue());
        assertEquals(newFirstName, updateUserResponse.getFirstName());
        assertEquals(newLastName, updateUserResponse.getLastName());
        assertEquals(newEmail, updateUserResponse.getEmailId());
    }

    @Test
    @Order(4)
    void changePassword_Success() {

        String confirmPassword = "user-pass32";

        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(NEW_PASSWORD, confirmPassword);
        ResponseDTO<Long> responseDTO = userController.changePassword(getAccessToken(PASSWORD), changePasswordDTO);

        assertThat(responseDTO.getData(), notNullValue());

        UserDTO userDTO = firebaseIntegration.getUser(USERNAME);

        assertFalse(Utils.checkPassword(PASSWORD, userDTO.getPassword()));
        assertTrue(Utils.checkPassword(NEW_PASSWORD, userDTO.getPassword()));
    }

    @Test
    @Order(5)
    void userLogout_Success() {

        String response = userController.logout(getAccessToken(NEW_PASSWORD));

        assertEquals(Constants.USER_LOGOUT_SUCCESSFUL, response);
    }

    @Test
    @Order(6)
    void deleteMyAccount_Success() {

        String accessToken = getAccessToken(NEW_PASSWORD);
        String updatedPassword = "user-pass32";

        DeleteUserDTO deleteUserDTO = new DeleteUserDTO(USERNAME, updatedPassword);
        String response = userController.deleteUser(accessToken, deleteUserDTO);

        assertEquals(Constants.USER_DELETED + " " + USERNAME, response);

        UserDTO userDTO = firebaseIntegration.getUser(USERNAME);

        assertTrue(Objects.isNull(userDTO));
    }

    //Private Methods
    private String getAccessToken(String password) {
        SignInDTO credentials = new SignInDTO();
        credentials.setUsername(USERNAME);
        credentials.setPassword(password);

        ResponseDTO<UserLoginDTO> response = userController.login(credentials);
        assertThat(response.getData().getAccessToken(), notNullValue());
        return response.getData().getAccessToken();
    }
}