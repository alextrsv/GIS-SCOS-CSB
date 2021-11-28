package ru.edu.online.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;
import ru.edu.online.entities.CacheStudent;
import ru.edu.online.entities.dto.*;
import ru.edu.online.entities.enums.ScosUserRole;
import ru.edu.online.entities.enums.UserRole;
import ru.edu.online.repositories.IValidateStudentCacheRepository;
import ru.edu.online.services.IUserDetailsService;
import ru.edu.online.utils.ScosApiUtils;
import ru.edu.online.utils.UserUtils;
import ru.edu.online.utils.VamApiUtils;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис данных пользователя
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements IUserDetailsService {

    private final IValidateStudentCacheRepository studentCashRepository;

    /** Время ожидания в случае отсутствия ответа */
    private static final int REQUEST_TIMEOUT = 1000;
    /** Веб клиент для доступа к DEV АПИ ГИС СЦОСа */
    private final WebClient devScosApiClient;
    /** Веб клиент для доступа к DEV АПИ ВАМа */
    private final WebClient devVamApiClient;

    @Autowired
    public UserDetailsServiceImpl(WebClient devScosApiClient, WebClient devVamApiClient,
                                  IValidateStudentCacheRepository studentCashRepository) {
        this.devScosApiClient = devScosApiClient;
        this.devVamApiClient = devVamApiClient;
        this.studentCashRepository = studentCashRepository;
    }

    /**
     * Получить роль пользователя.
     * @param userId идентификатор пользователя
     * @return роль.
     */
    @Override
    public UserRole getUserRole(String userId) {
        if (isSecurityOfficer(userId)) {
            return UserRole.SECURITY;
        }
        if (isUniversity(userId)) {
            return UserRole.ADMIN;
        }
        if (userId.equals("ba878477-1c00-4e3e-9a19-f61a147a2f83")) {
            return UserRole.TEACHER;
        }
        if (isStudent(userId)) {
            return UserRole.STUDENT;
        }

        return UserRole.UNDEFINED;
    }

    /**
     * Является ли пользователь охранником?
     * @param userId идентифиактор пользователя
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isSecurityOfficer(String userId) {
        return hasRole(userId, ScosUserRole.SECURITY_OFFICER);
    }

    /**
     * Является ли пользователь администратором ООВО?
     * @param userId идентификатор пользователя
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isUniversity(String userId) {
        return hasRole(userId, ScosUserRole.UNIVERSITY);
    }

    /**
     * Является ли пользователь администратором ГИС СЦОС?
     * @param userId идентификатор пользователя
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isSuperUser(String userId) {
        return hasRole(userId, ScosUserRole.SUPER_USER);
    }

    /**
     * Получить ОГРН организации админа
     * @param userId идентификатор пользователя
     * @return ОГРН организации админа
     */
    @Override
    public Optional<String> getAdminOrganizationOGRN(String userId) {
        UserDTO admin = ScosApiUtils.getUserDetails(devScosApiClient, userId);
        Optional<EmploymentDTO> employmentDTO = admin
                .getEmployments()
                .stream()
                .filter(e -> e.getRoles().contains("UNIVERSITY"))
                .findFirst();
        return employmentDTO.map(EmploymentDTO::getOgrn);
    }

    /**
     * Является ли пользователь студентом?
     * @param userId идентификатор пользователя
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isStudent(String userId) {
        Optional<CacheStudent> student;

        Optional<StudentDTO> studentDTO = getStudentByEmail(userId);
        if (studentDTO.isPresent()) {
            student = getStudentFromCacheByEmail(studentDTO.get());
            if (student.isPresent()) {
                return true;
            }
            if (saveStudentInCashByEmail(studentDTO.get())) {
                return true;
            }
            return true;
        }
        return false;
    }

    /**
     * Имеет ли роль?
     * @param userId идентификатор пользователя
     * @param role искомая роль
     * @return true - имеет, false - не имеет
     */
    private boolean hasRole(String userId, ScosUserRole role) {
        return Arrays
                .asList(
                        loadUserInfoByIdSync(userId)
                                .getRoles()
                )
                .contains(role.toString());
    }

    /**
     * Синхронный запрос к АПИ ГИС СЦОС на загрузку
     * информации о пользователе по id из principal
     * @param userId идентификатор пользователя
     * @return dto пользователя из ответа на запрос
     */
    private UserDetailsDTO loadUserInfoByIdSync(String userId) {
        return devScosApiClient
                .get()
                .uri(String.join("", "/users/", userId))
                .retrieve()
                .bodyToMono(UserDetailsDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block();
    }

    private Optional<StudentDTO> getStudentByEmail(String userId) {
        UserDetailsDTO userDetails = loadUserInfoByIdSync(userId);
        StudentsDTO students = VamApiUtils.getStudents("email", userDetails.getEmail(), devVamApiClient);
        return students
                .getResults()
                .stream()
                .filter(
                        student ->
                                student.getEmail().equals(userDetails.getEmail())
                )
                .findFirst();
    }

    /**
     * Получить данные для профиля пользователя
     * @param userId идентификатор польователя (доступно только для студента и препода)
     * @return профиль пользователя
     */
    @Override
    public Optional<UserProfileDTO> getUserProfile(String userId) {
        switch (getUserRole(userId)) {
            case STUDENT:
                return Optional.ofNullable(getStudentProfile(userId));
            case TEACHER:
                return Optional.of(getTeacherProfile());
            default:
                return Optional.empty();
        }
    }

    /**
     * Получить пользователей организации
     * @param userId идентификатор польователя
     * @return список пользователей из ООВО админа
     */
    @Override
    public Optional<ResponseDTO<UserDetailsDTO>> getUsersByOrganization(String userId,
                                                                        Long page,
                                                                        Long pageSize,
                                                                        String search) {
        UserDTO userDTO = ScosApiUtils.getUserDetails(devScosApiClient, userId);
        StudentsDTO students = VamApiUtils.getStudents(
                "organization_id",
                userDTO.getEmployments()
                        .stream()
                        .findFirst()
                        .get()
                        .getOgrn(),
                devVamApiClient
        );

        List<UserDetailsDTO> users = new ArrayList<>();

        for (StudentDTO student : students.getResults()) {
            UserDetailsDTO user = new UserDetailsDTO();
            user.setUserId(ScosApiUtils.getUserByEmail(devScosApiClient, student.getEmail()).getUser_id());
            user.setFirstName(student.getName());
            user.setLastName(student.getSurname());
            user.setPatronymicName(student.getMiddle_name());
            user.setEmail(student.getEmail());
            user.setRoles(new String[]{"STUDENT"});
            users.add(user);
        }

        if (Optional.ofNullable(search).isPresent()) {
            users = UserUtils.searchByEmail(users, search);
        }
        long usersCount = users.size();
        users = users.stream()
                .skip(pageSize * (page - 1))
                .limit(pageSize)
                .collect(Collectors.toList());

        return Optional.of(new ResponseDTO<>(
                page,
                pageSize,
                usersCount % pageSize == 0 ? usersCount / pageSize : usersCount / pageSize + 1,
                usersCount,
                users
        ));
    }

    private UserProfileDTO getStudentProfile(String userId) {
        UserDTO user = ScosApiUtils.getUserDetails(devScosApiClient, userId);
        StudentsDTO students = VamApiUtils.getStudents("email", user.getEmail(), devVamApiClient);
        Optional<StudentDTO> student = students.getResults()
                .stream()
                .filter(
                        s ->
                                s.getEmail().equals(user.getEmail())
                )
                .filter(s -> s.getStudy_year() != null)
                .findFirst();
        if (student.isPresent()) {
            Optional<OrganizationDTO> organization =
                    ScosApiUtils.getOrganizationByGlobalId(
                            devScosApiClient,
                            student.get().getOrganization_id()
                    );

            if (organization.isPresent()) {
                UserProfileDTO userProfile = new UserProfileDTO();
                userProfile.setFirstName(student.get().getName());
                userProfile.setLastName(student.get().getSurname());
                userProfile.setPatronymicName(student.get().getMiddle_name());
                userProfile.setStudyYear(student.get().getStudy_year());
                userProfile.setStudNumber("25643682");
                userProfile.setEducationForm("Бюджет");
                userProfile.setOrganizationFullName(organization.get().getFull_name());
                userProfile.setOrganizationShortName(organization.get().getShort_name());
                userProfile.setRole(UserRole.STUDENT);

                return userProfile;
            }
        }

        return null;
    }

    private UserProfileDTO getTeacherProfile() {
        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setOrganizationShortName("Университет ИТМО");
        userProfileDTO.setOrganizationFullName("Федеральное государственное автономное образовательное учреждение высшего образования «Национальный исследовательский университет ИТМО»");
        userProfileDTO.setFirstName("Преподаватель");
        userProfileDTO.setLastName("Тестовый");
        userProfileDTO.setPatronymicName("");
        userProfileDTO.setRole(UserRole.TEACHER);

        return userProfileDTO;
    }

    /**
     * Получить студента из кэша по почте
     * @param student dto студента
     * @return студент, если не найден - Optional.empty()
     */
    private Optional<CacheStudent> getStudentFromCacheByEmail(StudentDTO student) {
        return studentCashRepository.findByEmail(student.getEmail());
    }

    /**
     * Сохранить студента в кэш по почте студента
     * @param studentDTO студента из ВАМа
     */
    private boolean saveStudentInCashByEmail(StudentDTO studentDTO) {
        if (studentDTO.getEmail() != null) {
            studentCashRepository.save(
                    new CacheStudent(studentDTO.getEmail())
            );
            return true;
        }
        return false;
    }

    /**
     * Удалить устаревшие записи о валидированных студентах
     * @return успешно ли удаление?
     */
    @Override
    public boolean removeOldValidations() {
        return studentCashRepository.deleteByValidationDateBefore(LocalDate.now().minusMonths(1));
    }
}
