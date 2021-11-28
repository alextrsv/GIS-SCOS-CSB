package ru.edu.online.utils;

import ru.edu.online.entities.dto.UserDetailsDTO;

import java.util.List;
import java.util.stream.Collectors;

public class UserUtils {

    /**
     * Поиск пользователя по email
     * @param users список пользователей для поиска
     * @param email почта
     * @return результат поиска
     */
    public static List<UserDetailsDTO> searchByEmail(List<UserDetailsDTO> users, String email) {
        return users.stream().filter(user -> user.getEmail().contains(email)).collect(Collectors.toList());
    }
}
