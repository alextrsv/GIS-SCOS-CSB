package ru.edu.online.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.edu.online.entities.dto.*;
import ru.edu.online.entities.enums.ScosUserRole;
import ru.edu.online.entities.enums.UserRole;
import ru.edu.online.repositories.IValidateStudentCacheRepository;
import ru.edu.online.services.IScosAPIService;
import ru.edu.online.services.IUserDetailsService;
import ru.edu.online.services.IVamAPIService;
import ru.edu.online.utils.UserUtils;

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

    /** Репозиторий кэша студентов */
    private final IValidateStudentCacheRepository studentCashRepository;

    /** Сервис для работы с АПИ СЦОСа */
    private final IScosAPIService scosAPIService;
    /** Сервис для работы с АПИ ВАМа */
    private final IVamAPIService vamAPIService;

    @Autowired
    public UserDetailsServiceImpl(IValidateStudentCacheRepository studentCashRepository,
                                  IVamAPIService vamAPIService,
                                  IScosAPIService scosAPIService) {
        this.studentCashRepository = studentCashRepository;
        this.scosAPIService = scosAPIService;
        this.vamAPIService = vamAPIService;
    }

    /**
     * Получить роль пользователя.
     * @param userId идентификатор пользователя
     * @return роль.
     */
    @Override
    public UserRole getUserRole(String userId) {
        if (hasRole(userId, ScosUserRole.SUPER_USER)) {
            return UserRole.SUPER_USER;
        }
        if (hasRole(userId, ScosUserRole.SECURITY_OFFICER)) {
            return UserRole.SECURITY;
        }
        if (hasRole(userId, ScosUserRole.UNIVERSITY)) {
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
        return getUserRole(userId) == UserRole.SECURITY;
    }

    /**
     * Является ли пользователь администратором ООВО?
     * @param userId идентификатор пользователя
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isUniversity(String userId) {
        return getUserRole(userId) == UserRole.ADMIN;
    }

    /**
     * Является ли пользователь администратором ГИС СЦОС?
     * @param userId идентификатор пользователя
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isSuperUser(String userId) {
        return getUserRole(userId) == UserRole.SUPER_USER;
    }

    /**
     * Получить global_id организации админа
     * @param userId идентификатор пользователя
     * @return global_id организации админа
     */
    @Override
    public Optional<String> getUserOrganizationGlobalId(String userId) {
        Optional<UserDTO> admin = scosAPIService.getUserDetails(userId);
        if (admin.isPresent()) {
            Optional<EmploymentDTO> employmentDTO = admin.get()
                    .getEmployments()
                    .stream()
                    .filter(e -> e.getRoles().contains("UNIVERSITY"))
                    .findFirst();
            if (employmentDTO.isPresent()) {
                Optional<OrganizationDTO> adminOrganization =
                        scosAPIService.getOrganization(employmentDTO.get().getOgrn());
                if (adminOrganization.isPresent()) {
                    return adminOrganization.get().getOrganizationId();
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Является ли пользователь студентом?
     * @param userId идентификатор пользователя
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isStudent(String userId) {

        Optional<UserDTO> userDTO = scosAPIService.getUserDetails(userId);
        if (userDTO.isPresent()) {
            Optional<StudentDTO> studentDTO = vamAPIService.getStudentByEmail(userDTO.get());
            return studentDTO.isPresent();
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
        return scosAPIService.getUserDetails(userId).map(
                        userDTO -> userDTO.getRoles().contains(role.getValue())
                )
                .orElse(false);
    }

    /**
     * Получить данные для профиля пользователя
     * @param userId идентификатор польователя (доступно только для студента и препода)
     * @return профиль пользователя
     */
    @Override
    public Optional<UserProfileDTO> getUserProfile(String userId) {
        switch (getUserRole(userId)) {
            case ADMIN:
            case SECURITY:
            case SUPER_USER:
                return getEmploymentProfile(userId, getUserRole(userId));
            case STUDENT:
                return Optional.ofNullable(getStudentProfile(userId));
            case TEACHER:
                return Optional.of(getTeacherProfile());
            default:
                return Optional.empty();
        }
    }

    private Optional<UserProfileDTO> getEmploymentProfile(String userId, UserRole role) {
        Optional<UserDTO> userScosInfo = scosAPIService.getUserDetails(userId);
        if (userScosInfo.isPresent()) {
            Optional<UserByFIOResponseDTO> userByFIO =
                    scosAPIService.getUserByFIO(
                            userScosInfo.get().getFirst_name(),
                            userScosInfo.get().getLast_name()
                    );
            if (userByFIO.isPresent()) {
                Optional<UserDTO> userInfo =
                        Arrays.stream(userByFIO.get().getData())
                                .filter(user -> user.getUser_id().equals(userId))
                                .findFirst();

                UserProfileDTO userProfile = new UserProfileDTO();

                if (userInfo.isPresent()) {
                    Optional<EmploymentDTO> employment =
                            userScosInfo.get().getEmployments()
                                    .stream()
                                    .filter(e -> e.getRoles().contains(role.getValue()))
                                    .findFirst();
                    if (employment.isPresent()) {
                        Optional<OrganizationDTO> organization =
                                scosAPIService.getOrganization(
                                        employment.get().getOgrn()
                                );

                        userProfile.setEmail(userInfo.get().getEmail());
                        userProfile.setRole(role);
                        userProfile.setPhotoURL(userInfo.get().getPhoto_url());
                        userProfile.setFirstName(userInfo.get().getFirst_name());
                        userProfile.setLastName(userInfo.get().getLast_name());
                        userProfile.setPatronymicName(userInfo.get().getPatronymic_name());

                        if (organization.isPresent()) {
                            userProfile.setOrganizationFullName(organization.get().getFull_name());
                            userProfile.setOrganizationShortName(organization.get().getShort_name());
                        }
                    }
                }

                return Optional.of(userProfile);
            }
        }

        return Optional.empty();
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
        Optional<UserDTO> user = scosAPIService.getUserDetails(userId);
        Optional<EmploymentDTO> employmentDTO = user.orElseThrow().getEmployments()
                .stream()
                .findFirst();
        Optional<StudentsDTO> students = vamAPIService.getStudents(
                "organization_id",
                employmentDTO.orElseThrow().getOgrn()
        );
        if (students.isPresent()) {
            List<UserDetailsDTO> users = new ArrayList<>();

            for (StudentDTO student : students.get().getResults()) {
                UserDetailsDTO userDetails = new UserDetailsDTO();
                user = Arrays.stream(
                                scosAPIService.getUserByFIO(
                                        student.getName(),
                                        student.getSurname()).orElseThrow().getData()
                        )
                        .filter(u -> u.getUser_id().equals(
                                scosAPIService.getUserByEmail(
                                        student.getEmail()
                                ).orElseThrow().getUser_id())
                        )
                        .findFirst();
                if (user.isPresent()) {
                    userDetails.setUserId(user.get().getUser_id());
                    userDetails.setFirstName(student.getName());
                    userDetails.setLastName(student.getSurname());
                    userDetails.setPatronymicName(student.getMiddle_name());
                    userDetails.setEmail(student.getEmail());
                    userDetails.setRoles(new String[]{"STUDENT"});
                    userDetails.setPhotoURL(user.get().getPhoto_url());
                    userDetails.setUserOrganizationShortName(
                            scosAPIService.getOrganizationByGlobalId(
                                    student.getOrganization_id()
                            ).orElseThrow().getShort_name());
                    users.add(userDetails);
                }
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

        return Optional.empty();
    }

    private UserProfileDTO getStudentProfile(String userId) {
        Optional<UserDTO> user = scosAPIService.getUserDetails(userId);

        user = Arrays.stream(
                scosAPIService.getUserByFIO(
                        user.orElseThrow().getFirst_name(),
                        user.orElseThrow().getLast_name())
                        .orElseThrow().getData())
                .filter(u -> u.getUser_id().equals(userId))
                .findFirst();

        Optional<StudentsDTO> students =
                vamAPIService.getStudents(
                        "email",
                        user.orElseThrow().getEmail()
                );
        Optional<UserDTO> finalUser = user;
        Optional<StudentDTO> student = students.orElseThrow().getResults()
                .stream()
                .filter(
                        s ->
                                s.getEmail().equals(finalUser.orElseThrow().getEmail())
                )
                .filter(s -> s.getStudy_year() != null)
                .findFirst();
        if (student.isPresent()) {
            Optional<OrganizationProfileDTO> organization =
                    scosAPIService.getOrganizationByGlobalId(
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
                userProfile.setPhotoURL(user.orElseThrow().getPhoto_url());
                userProfile.setEmail(user.orElseThrow().getEmail());

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
     * Удалить устаревшие записи о валидированных студентах
     * @return успешно ли удаление?
     */
    @Override
    public boolean removeOldValidations() {
        return studentCashRepository.deleteByValidationDateBefore(LocalDate.now().minusMonths(1));
    }
}
