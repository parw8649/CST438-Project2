package com.wishlist.cst438project2.controller;

import com.wishlist.cst438project2.common.Constants;
import com.wishlist.cst438project2.common.TokenManager;
import com.wishlist.cst438project2.dto.*;
import com.wishlist.cst438project2.enums.RoleType;
import com.wishlist.cst438project2.exception.BadRequestException;
import com.wishlist.cst438project2.exception.UnauthorizedException;
import com.wishlist.cst438project2.service.AdminService;
import com.wishlist.cst438project2.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
/**
 * API endpoints for handling the Admin functionalities
 *
 * references:
 *     - https://spring.io/projects/spring-boot
 *     - https://stackoverflow.com/questions/29479814/spring-mvc-or-spring-boot
 * @author Chaitanya Parwatkar
 * @version %I% %G%
 */

@CrossOrigin
@RestController
@RequestMapping("/v1/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private ItemService itemService;

    @GetMapping("/users")
    public List<UserDTO> getAllUsers(@RequestHeader String accessToken) {

        log.info("AdminController: Starting getAllUsers");

        try {

            UserTokenDTO userTokenDTO = tokenManager.getUser(accessToken);

            if(Objects.isNull(userTokenDTO) || !userTokenDTO.getRole().equals(RoleType.ADMIN.getValue()))
                throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);

            List<UserDTO> userDTOList = adminService.getAllUsers();

            log.info("UserController: Exiting saveUser");

            return userDTOList;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * This API is used for deleting user from database
     * @param username, of the user whose account is to be deleted.
     */
    @DeleteMapping("/deleteUser")
    public String deleteUser(@RequestHeader String accessToken, @RequestParam String username) {

        log.info("AdminController: Starting deleteUser");

        try{
            UserTokenDTO userTokenDTO = tokenManager.getUser(accessToken);

            if(Objects.isNull(userTokenDTO) || !userTokenDTO.getRole().equals(RoleType.ADMIN.getValue()))
                throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);

            if(Objects.isNull(username) || username.isEmpty())
                throw new BadRequestException();

            adminService.deleteUser(username);

            log.info("AdminController: Exiting deleteUser");

            return Constants.USER_DELETED + " " + username;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * This API is used for user registration by admin
     * @param signUpDTO
     * @return user creation timestamp
     */
    @PostMapping("/createUser")
    public String createUser(@RequestHeader String accessToken, @RequestBody SignUpDTO signUpDTO) {

        log.info("AdminController: Starting createUser");

        try {
            UserTokenDTO userTokenDTO = tokenManager.getUser(accessToken);

            if(Objects.isNull(userTokenDTO) || !userTokenDTO.getRole().equals(RoleType.ADMIN.getValue()))
                throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);

            if (Objects.isNull(signUpDTO))
                throw new BadRequestException();

            String responseTimestamp = adminService.createUser(signUpDTO);

            log.info("AdminController: Exiting createUser");

            return Objects.nonNull(responseTimestamp) && !responseTimestamp.isEmpty()
                    ? Constants.USER_CREATED : Constants.ERROR_UNABLE_TO_CREATE_USER;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    @PostMapping("/login")
    public ResponseDTO<UserLoginDTO> login(@RequestBody SignInDTO signInDTO) {

        log.info("AdminController: Starting login");

        ResponseDTO<UserLoginDTO> responseDTO = new ResponseDTO<>();
        UserLoginDTO userLoginDTO = null;
        String message = HttpStatus.UNAUTHORIZED.toString();
        int httpStatusCode;
        try {

            if (Objects.isNull(signInDTO))
                throw new BadRequestException();

            userLoginDTO = adminService.login(signInDTO);
            if(Objects.isNull(userLoginDTO))
                throw new UnauthorizedException();

            httpStatusCode = HttpStatus.OK.value();
            message = Constants.USER_LOGIN_SUCCESSFUL;
            log.info("AdminController: Exiting login");

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            httpStatusCode = HttpStatus.UNAUTHORIZED.value();
        }

        responseDTO.setStatus(httpStatusCode);
        responseDTO.setData(userLoginDTO);
        responseDTO.setMessage(message);
        return responseDTO;
    }

    /**
     * POST request to create an item in the database.
     * returns item creation timestamp
     */
    @PostMapping("/createItem")
    public String createItem(@RequestHeader String accessToken, @RequestBody ItemDTO itemDTO, @RequestParam String username) {
        log.info("ItemController: Starting createItem");
        try {
            UserTokenDTO userTokenDTO = tokenManager.getUser(accessToken);

            if(Objects.isNull(userTokenDTO) || !userTokenDTO.getRole().equals(RoleType.ADMIN.getValue()))
                throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);

            if (Objects.isNull(itemDTO) || (itemDTO.getName().isBlank() || Objects.isNull(itemDTO.getUserId()))) {
                throw new BadRequestException();
            } else {
                String timestamp = itemService.createItem(itemDTO, username);
                log.info("ItemController: exiting successful createItem");
                return timestamp;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * DELETE request to remove the item associated with a given user ID and item name
     * returns timestamp of successful deletion
     */
    @DeleteMapping("/removeItems")
    public String removeItem(@RequestHeader String accessToken, @RequestParam String itemName, @RequestParam Long userId) {

        UserTokenDTO userTokenDTO = tokenManager.getUser(accessToken);

        if(Objects.isNull(userTokenDTO) || !userTokenDTO.getRole().equals(RoleType.ADMIN.getValue()))
            throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);

        log.info("ItemController: Starting removeItem");
        log.info("ItemController: removeItem:\n    name: {}\n    userId: {}", itemName, userId);
        return adminService.removeItem(itemName, userId);
    }

    /**
     * This API is used for updating user information in database
     * @param userDTO, whose details is to be updated
     * @return user creation timestamp
     */
    @PutMapping("/updateUser")
    public UserDTO updateUser(@RequestHeader String accessToken, @RequestBody UserDTO userDTO) {

        log.info("UserController: Starting updateUser");

        try {

            UserTokenDTO userTokenDTO = tokenManager.getUser(accessToken);

            if(Objects.isNull(userTokenDTO) || !userTokenDTO.getRole().equals(RoleType.ADMIN.getValue()))
                throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);

            if(Objects.isNull(userDTO))
                throw new BadRequestException();

            userDTO = adminService.updateUser(userDTO);

            log.info("UserController: Exiting updateUser");

            return userDTO;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * This API is used for updating an existing Item information in the database.
     * @param accessToken Admin's access token.
     * @param oldItemName Name of the item to be updated.
     * @param updatedItemDTO Details of the item to be updated.
     * @return timestamp, when the item was updated.
     */
    @PatchMapping("/updateItem")
    public String updateItem(@RequestHeader String accessToken, @RequestParam(name = "itemName") String oldItemName, @RequestBody ItemDTO updatedItemDTO) {
        log.info("AdminController: Starting updateItem");
        log.info(String.format("AdminController: updateItem:\n    old name: %s\n    userId: %s", oldItemName, updatedItemDTO.getUserId()));

        UserTokenDTO userTokenDTO = tokenManager.getUser(accessToken);
        if(Objects.isNull(userTokenDTO) || !userTokenDTO.getRole().equals(RoleType.ADMIN.getValue()))
            throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);

        return itemService.updateItem(oldItemName, updatedItemDTO);
    }

    /**
     * This API is used for fetching all items from database
     * @param accessToken Admin's access token
     * @return List of ItemDTOs
     */
    @GetMapping("/getAllItems")
    public List<ItemDTO> getAllItems(@RequestHeader String accessToken) {
        log.info("AdminController: Starting getAllItems");
        try {

            UserTokenDTO userTokenDTO = tokenManager.getUser(accessToken);
            if(Objects.isNull(userTokenDTO) || !userTokenDTO.getRole().equals(RoleType.ADMIN.getValue()))
                throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);

            List<ItemDTO> collection = itemService.getAllItems();
            log.info("AdminController: Exiting successful getAllItems");
            return collection;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * This API is used to search a specific item list from given keyword
     * @param accessToken Admin's access token
     * @param keywords used to search related items from the database/user's wishlist
     * @return List of itemDTOs (item details)
     */
    @GetMapping("/searchItem")
    public List<ItemDTO> searchAllItems(@RequestHeader String accessToken, @RequestParam("search") List<String> keywords) {
        log.info("AdminController: Starting searchAllItems");
        log.info(String.format("AdminController: searchAllItems:\n     keywords: %s", keywords));

        UserTokenDTO userTokenDTO = tokenManager.getUser(accessToken);
        if(Objects.isNull(userTokenDTO))
            throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);

        return itemService.getSearchAllItems(keywords);
    }
}
