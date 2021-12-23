package ru.edu.online.utils;

import ru.edu.online.entities.dto.*;
import ru.edu.online.entities.enums.UserRole;

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
    public static Optional<ResponseDTO<UserDTO>> aggregateUserWithPaginationAndSearch(
            List<UserDTO> users,
            long page,
            long usersPerPage,
            String search) {
        if (Optional.ofNullable(search).isPresent()) {
            users = searchByEmail(users, search);
        }
        long usersCount = users.size();
        users = paginateUsers(users, page, usersPerPage);

        return Optional.of(new ResponseDTO<>(
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

    /**
     * Получить UserProfileDTO из UserDTO
     * @param user dto пользователя
     * @param role роль пользователя
     * @param userOrganization организация пользователя
     * @return профиль пользователя
     */
    public static UserProfileDTO getUserProfileDTOFromUserDTO(UserDTO user,
                                                              UserRole role,
                                                              OrganizationDTO userOrganization) {
        UserProfileDTO userProfile = new UserProfileDTO();

        userProfile.setEmail(user.getEmail());
        userProfile.setRole(role);
        userProfile.setPhotoURL(user.getPhoto_url());
        userProfile.setFirstName(user.getFirst_name());
        userProfile.setLastName(user.getLast_name());
        userProfile.setPatronymicName(user.getPatronymic_name());
        userProfile.setOrganizationFullName(userOrganization.getFull_name());
        userProfile.setOrganizationShortName(userOrganization.getShort_name());

        return userProfile;
    }

    /**
     * Получить UserProfileDTO из StudentDTO
     * @param student dto студента из ВАМ
     * @param organization организация студента
     * @param studNumber номер студенческого билета
     * @param photoURL ссылка на фото
     * @param email почта
     * @return профиль студента
     */
    public static UserProfileDTO getUserProfileDTOFromStudentDTO(
            StudentDTO student,
            OrganizationProfileDTO organization,
            String studNumber,
            String photoURL,
            String email) {

        UserProfileDTO userProfile = new UserProfileDTO();

        userProfile.setFirstName(student.getName());
        userProfile.setLastName(student.getSurname());
        userProfile.setPatronymicName(student.getMiddle_name());
        userProfile.setStudyYear(student.getStudy_year());
        userProfile.setStudNumber(studNumber);
        userProfile.setOrganizationFullName(organization.getFull_name());
        userProfile.setOrganizationShortName(organization.getShort_name());
        userProfile.setRole(UserRole.STUDENT);
        userProfile.setPhotoURL(photoURL);
        userProfile.setEmail(email);

        return userProfile;
    }

    /**
     * Получить пользователей конкретной страницы с поиском
     * @param users список пользователей
     * @param page номер страницы
     * @param usersPerPage количество пользователей на странице
     * @param search поиск (опционально)
     * @return список пользователей по параметрам
     */
    public static ResponseDTO<UserDTO> getUsersWithPaginationAndSearch(List<UserDTO> users,
                                                                       long page,
                                                                       long usersPerPage,
                                                                       String search) {
        if (Optional.ofNullable(search).isPresent()) {
            users = searchByEmail(users, search);
        }
        long usersCount = users.size();
        users = users.stream()
                .skip(usersPerPage * (page - 1))
                .limit(usersPerPage)
                .collect(Collectors.toList());

        return new ResponseDTO<>(
                page,
                usersPerPage,
                usersCount % usersPerPage == 0 ?
                        usersCount / usersPerPage :
                        usersCount / usersPerPage + 1,
                usersCount,
                users
        );
    }
}
