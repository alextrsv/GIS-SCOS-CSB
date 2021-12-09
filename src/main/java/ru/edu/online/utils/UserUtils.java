package ru.edu.online.utils;

import ru.edu.online.entities.dto.UserDTO;

import java.util.List;
import java.util.stream.Collectors;

public class UserUtils {

    /**
     * Поиск пользователя по email
     * @param users список пользователей для поиска
     * @param email почта
     * @return результат поиска
     */
    public static List<UserDTO> searchByEmail(List<UserDTO> users, String email) {
        return users.stream().filter(user -> user.getEmail().contains(email)).collect(Collectors.toList());
    }

    /**
     * Сделать пагинацию для списка пользователей с сортировкой по дате создания
     * @param page номер страницы
     * @param usersPerPage количество пользователей на странице
     * @param users пользователеи
     * @return страница пользователей
     */
    public static List<UserDTO> paginateUsers(List<UserDTO> users, long page, long usersPerPage) {
        return users
                .stream()
                .skip(usersPerPage * (page - 1))
                .limit(usersPerPage)
                .collect(Collectors.toList());
    }
}
