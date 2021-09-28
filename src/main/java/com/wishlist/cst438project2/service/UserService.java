package com.wishlist.cst438project2.service;

import com.wishlist.cst438project2.dto.UserDTO;

public interface UserService {

    String saveUser(UserDTO userDTO);
    UserDTO updateUser(UserDTO userDTO);
    void deleteUser(String username);
}