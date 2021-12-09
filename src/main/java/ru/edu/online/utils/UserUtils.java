package ru.edu.online.utils;

import ru.edu.online.entities.dto.GenericResponseDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.dto.UserProfileDTO;

import java.util.List;
import java.util.Optional;
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

    /**
     * Отобрать пользователей по заданным критериям
     * @param users список пользователей для работы
     * @param page номер страницы для вывода
     * @param usersPerPage количество пользователей на странице
     * @param search поиск (опционально)
     * @return список пользователей по заданным критериям
     */
    public static Optional<GenericResponseDTO<UserDTO>> aggregateUserWithPaginationAndSearch(
            List<UserDTO> users,
            long page,
            long usersPerPage,
            String search) {
        if (Optional.ofNullable(search).isPresent()) {
            users = searchByEmail(users, search);
        }
        long usersCount = users.size();
        users = paginateUsers(users, page, usersPerPage);

        return Optional.of(new GenericResponseDTO<>(
                page,
                usersPerPage,
                usersCount % usersPerPage == 0 ? usersCount / usersPerPage : usersCount / usersPerPage + 1,
                usersCount,
                users
        ));
    }

    /**
     * Получить UserDTO из UserProfileDTO
     * @param userProfileDTO dto профиля пользователя
     * @param userId идентификатор пользователя
     * @return dto пользователя
     */
    public static UserDTO getUserDTOFromUserProfileDTO(UserProfileDTO userProfileDTO, String userId) {
        UserDTO user = new UserDTO();

        user.setUser_id(userId);
        user.setFirst_name(userProfileDTO.getFirstName());
        user.setLast_name(userProfileDTO.getLastName());
        user.setPatronymic_name(userProfileDTO.getPatronymicName());
        user.setEmail(userProfileDTO.getEmail());
        user.setPhoto_url(userProfileDTO.getPhotoURL());
        user.setUserOrganizationShortName(userProfileDTO.getOrganizationShortName());

        return user;
    }

}
