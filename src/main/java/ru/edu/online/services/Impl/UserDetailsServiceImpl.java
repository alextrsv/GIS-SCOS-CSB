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

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
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
     * @param principal атворизация пользователя
     * @return роль.
     */
    @Override
    public UserRole getUserRole(Principal principal) {
        if (isSecurityOfficer(principal)) {
            return UserRole.SECURITY;
        }
        if (isUniversity(principal)) {
            return UserRole.ADMIN;
        }
        if (principal.getName().equals("ba878477-1c00-4e3e-9a19-f61a147a2f83")) {
            return UserRole.TEACHER;
        }
        if (isStudent(principal)) {
            return UserRole.STUDENT;
        }

        return UserRole.UNDEFINED;
    }

    /**
     * Является ли пользователь охранником?
     * @param principal информация о пользователе
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isSecurityOfficer(Principal principal) {
        return hasRole(principal, ScosUserRole.SECURITY_OFFICER);
    }

    /**
     * Является ли пользователь администратором ООВО?
     * @param principal информация о пользователе
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isUniversity(Principal principal) {
        return hasRole(principal, ScosUserRole.UNIVERSITY);
    }

    /**
     * Является ли пользователь администратором ГИС СЦОС?
     * @param principal информация о пользователе
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isSuperUser(Principal principal) {
        return hasRole(principal, ScosUserRole.SUPER_USER);
    }

    /**
     * Является ли пользователь студентом?
     * @param principal информация о пользователе
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isStudent(Principal principal) {
        Optional<CacheStudent> student =
                getStudentFromCacheByScosId(UUID.fromString(principal.getName()));

        if (student.isPresent()) {
            return true;
        } else {
            Optional<StudentDTO> studentDTO = getStudentByEmail(principal);
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
        }
        return false;
    }

    /**
     * Имеет ли роль?
     * @param principal информация о пользователе
     * @param role искомая роль
     * @return true - имеет, false - не имеет
     */
    private boolean hasRole(Principal principal, ScosUserRole role) {
        return Arrays
                .asList(
                        loadUserInfoByIdSync(principal)
                                .getRoles()
                )
                .contains(role.toString());
    }

    /**
     * Синхронный запрос к АПИ ГИС СЦОС на загрузку
     * информации о пользователе по id из principal
     * @param principal информация о пользователе
     * @return dto пользователя из ответа на запрос
     */
    private UserDetailsDTO loadUserInfoByIdSync(final Principal principal) {
        return devScosApiClient
                .get()
                .uri(String.join("", "/users/", principal.getName()))
                .retrieve()
                .bodyToMono(UserDetailsDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block();
    }

    private Optional<StudentDTO> getStudentByEmail(final Principal principal) {
        UserDetailsDTO userDetails = loadUserInfoByIdSync(principal);
        StudentsDTO students = getStudents("email", userDetails.getEmail());
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
     * Получить список студентов по одному параметру
     * @param parameter один из допустимых параметров для поиска:
     * inn - ИНН,
     * snils - СНИЛС,
     * email - почта,
     * scos_id - идентификатор в СЦОСе,
     * study_year - курс обучения студента,
     * filter - фильтр для поиска по ФИО (прим. - ван)
     * organization_id - идентификатор организации в СЦОС или её ОГРН,
     * from_date - дата, начиная с которой, будут отображаться изменения
     * @param value знаение параметра поиска
     * @return результат поиска по заданному параметру
     */
    private StudentsDTO getStudents(String parameter, String value) {
        return devVamApiClient
                .get()
                .uri(String.join("", "/students?", parameter, "=", value))
                .retrieve()
                .bodyToMono(StudentsDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block();
    }

    /**
     * Получить данные для профиля пользователя
     * @param principal авторизация польователя (доступно только для студента и препода)
     * @return профиль пользователя
     */
    @Override
    public Optional<UserProfileDTO> getUserProfile(Principal principal) {
        switch (getUserRole(principal)) {
            case STUDENT:
                return Optional.ofNullable(getStudentProfile(principal));
            case TEACHER:
                return Optional.of(getTeacherProfile());
            default:
                return Optional.empty();
        }
    }

    /**
     * Получить пользователей организации
     * @param principal авторизация админа
     * @return список пользователей из ООВО админа
     */
    @Override
    public Optional<List<UserDetailsDTO>> getUsersByOrganization(Principal principal,
                                                                 Long page,
                                                                 Long pageSize,
                                                                 Optional<String> search) {
        UserDTO userDTO = ScosApiUtils.getUserDetails(devScosApiClient, principal);
        StudentsDTO students = getStudents(
                "organization_id",
                userDTO.getEmployments()
                        .stream()
                        .findFirst()
                        .get()
                        .getOgrn());

        List<UserDetailsDTO> users = new ArrayList<>();

        for (StudentDTO student : students.getResults()) {
            UserDetailsDTO user = new UserDetailsDTO();
            user.setUserId(student.getId());
            user.setFirstName(student.getName());
            user.setLastName(student.getSurname());
            user.setPatronymicName(student.getMiddle_name());
            user.setEmail(student.getEmail());
            user.setRoles(new String[]{"STUDENT"});
            users.add(user);
        }

        return search.map(s -> UserUtils.searchByEmail(users, s).
                stream()
                .skip(pageSize * (page - 1))
                .limit(pageSize)
                .collect(Collectors.toList())).or(() -> Optional.of(users.stream()
                .skip(pageSize * (page - 1))
                .limit(pageSize)
                .collect(Collectors.toList())
        ));
    }

    private UserProfileDTO getStudentProfile(Principal principal) {
        UserDTO user = ScosApiUtils.getUserDetails(devScosApiClient, principal);
        StudentsDTO students = getStudents("email", user.getEmail());
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
     * Получить студента из кэша по идентификатору СЦОСа
     * @param id идентификатор СЦОСа
     * @return студент, если не найден - Optional.empty()
     */
    private Optional<CacheStudent> getStudentFromCacheByScosId(UUID id) {
        return studentCashRepository.findByScosId(id);
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
