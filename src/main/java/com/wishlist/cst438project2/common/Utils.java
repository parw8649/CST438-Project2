package com.wishlist.cst438project2.common;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * class utils
 * @author Chaitanya Parwatkar
 * @version %I% %G%
 */

@Component
public class Utils {

    public static String encodePassword(String password) {
        password = BCrypt.hashpw(password, BCrypt.gensalt());
        return password;
    }

    public static boolean checkPassword(String password, String dbPassword) {
        return BCrypt.checkpw(password, dbPassword);
    }

    public static boolean validatePassword(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public static Long generateId() {
        return new Date().getTime();
    }
}
