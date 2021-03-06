package com.wishlist.cst438project2.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;
import com.wishlist.cst438project2.common.Constants;
import com.wishlist.cst438project2.common.TokenManager;
import com.wishlist.cst438project2.common.Utils;
import com.wishlist.cst438project2.document.Item;
import com.wishlist.cst438project2.document.User;
import com.wishlist.cst438project2.dto.*;
import com.wishlist.cst438project2.enums.RoleType;
import com.wishlist.cst438project2.exception.BadRequestException;
import com.wishlist.cst438project2.exception.ExternalServerException;
import com.wishlist.cst438project2.exception.UnauthorizedException;
import com.wishlist.cst438project2.integration.FirebaseIntegration;
import com.wishlist.cst438project2.service.AdminService;
import com.wishlist.cst438project2.service.ItemService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private FirebaseIntegration firebaseIntegration;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private ItemService itemService;

    @Override
    public List<UserDTO> getAllUsers() {

        log.info("AdminServiceImpl: Starting getAllUsers");

        List<UserDTO> userDTOList = firebaseIntegration.getAllUsers();

        log.info("AdminServiceImpl: Exiting getAllUsers");

        return userDTOList;
    }

    @SneakyThrows
    @Override
    public void deleteUser(String username) {

        log.info("AdminServiceImpl: Starting deleteUser");

        User user = fetchUser(username);

        firebaseIntegration.deleteUser(user.getUsername());

        log.info("AdminServiceImpl: Exiting deleteUser");
    }

    @SneakyThrows
    @Override
    public String createUser(SignUpDTO signUpDTO) {

        log.info("AdminServiceImpl: Starting createUser");

        UserDTO dbUserDTO = firebaseIntegration.getUser(signUpDTO.getUsername());

        User user;
        if(Objects.isNull(dbUserDTO)) {
            user = modelMapper.map(signUpDTO, User.class);
            user.setUserId(firebaseIntegration.getAllUsers().size() + 1L);
        } else
            throw new BadRequestException(Constants.ERROR_USER_ALREADY_EXISTS.replace(Constants.KEY_USERNAME, signUpDTO.getUsername()));

        user.setPassword(Utils.encodePassword(user.getPassword()));

        ApiFuture<WriteResult> collectionApiFuture = firebaseIntegration.dbFirestore.collection(Constants.DOCUMENT_USER).document(user.getUsername()).set(user);

        String responseTimestamp = collectionApiFuture.get().getUpdateTime().toString();

        log.info("ResponseTimestamp: {}", responseTimestamp);

        log.info("UserServiceImpl: Exiting saveUser");

        return responseTimestamp;
    }

    @Override
    public UserLoginDTO login(SignInDTO signInDTO) {

        log.info("AdminServiceImpl: Starting login");

        User user = fetchUser(signInDTO.getUsername());

        if (!user.getRole().getValue().equals(RoleType.ADMIN.getValue()))
            throw new UnauthorizedException(Constants.ERROR_INVALID_TOKEN);

        String accessToken = null;

        if (Utils.checkPassword(signInDTO.getPassword(), user.getPassword())) {
            log.info(Constants.USER_LOGIN_SUCCESSFUL);
            accessToken = tokenManager.generateToken(user);
        }

        UserLoginDTO userLoginDTO = null;
        if (Objects.nonNull(accessToken) && !accessToken.isEmpty())
            userLoginDTO = new UserLoginDTO(user.fetchUserDTO(), accessToken);

        log.info("AdminServiceImpl: Exiting login");
        return userLoginDTO;
    }

    /**
     * remove the item associated with a given user ID and item name
     * returns timestamp of deletion
     */
    @Override
    public String removeItem(String name, Long userId) {
        log.info("AdminServiceImpl: Starting removeItem");
        String docId = firebaseIntegration.getItemDocId(name, userId);
        return  firebaseIntegration.removeItem(docId);
    }

    @SneakyThrows
    @Override
    public UserDTO updateUser(UserDTO userDTO) {

        log.info("AdminServiceImpl: Starting updateUser");

        User user = fetchUser(userDTO.getUsername());

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmailId(userDTO.getEmailId());

        if(Objects.nonNull(userDTO.getRole()))
            user.setRole(RoleType.valueOf(userDTO.getRole()));

        if(Objects.nonNull(userDTO.getPassword()))
            user.setPassword(Utils.encodePassword(userDTO.getPassword()));

        ApiFuture<WriteResult> collectionApiFuture = firebaseIntegration.dbFirestore.collection(Constants.DOCUMENT_USER).document(user.getUsername()).set(user);

        String responseTimestamp = collectionApiFuture.get().getUpdateTime().toString();

        if(responseTimestamp.isEmpty()) {
            throw new ExternalServerException(Constants.ERROR_UNABLE_TO_UPDATE_USER);
        }

        log.info("AdminServiceImpl: Exiting updateUser");

        return user.fetchUserDTO();
    }

    //Private Methods
    private User fetchUser(String username) {

        UserDTO dbUserDTO = firebaseIntegration.getUser(username);

        if(Objects.isNull(dbUserDTO)) {
            throw new BadRequestException(Constants.ERROR_USER_DOES_NOT_EXISTS.replace(Constants.KEY_USERNAME, username));
        }

        return modelMapper.map(dbUserDTO, User.class);
    }

    /**
     * returns the item found in database by given name
     */
    private Item fetchItem(String name, long userId) {
        ItemDTO dbItemDTO = firebaseIntegration.getItem(name, userId);

        if(Objects.isNull(dbItemDTO)) {
            return null;
        }

        return modelMapper.map(dbItemDTO, Item.class);
    }
}
