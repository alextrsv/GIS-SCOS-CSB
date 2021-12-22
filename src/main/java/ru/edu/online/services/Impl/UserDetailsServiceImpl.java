package ru.edu.online.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.edu.online.entities.CacheStudent;
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

/**
 * Сервис данных пользователя
 */
@Slf4j
@Service
@EnableScheduling
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
     * Является ли пользователь студентом?
     * @param userId идентификатор пользователя
     * @return true/false в зависимости от роли пользователя
     */
    @Override
    public boolean isStudent(String userId) {
        Optional<CacheStudent> student;

        Optional<StudentDTO> studentDTO =
                vamAPIService.getStudentByEmail(
                        scosAPIService.getUserDetails(userId).orElseThrow()
                );
        if (studentDTO.isPresent()) {
            student = getStudentFromCacheByEmailAndScosId(studentDTO.get().getEmail(), userId);
            if (student.isPresent()) {
                return true;
            }
            saveStudentInCashByEmailAndScosId(studentDTO.get().getEmail(), userId);
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
                return getStudentProfile(userId);
            default:
                return Optional.empty();
        }
    }

    /**
     * Получить профиль сотрудника огранизации
     * (Админ ООВО, Охранник, Супер - пользователь)
     * @param userId идентификатор пользователя
     * @param role роль пользователя
     * @return профиль пользователя
     */
    private Optional<UserProfileDTO> getEmploymentProfile(String userId, UserRole role) {
        Optional<UserDTO> user = scosAPIService.getUserDetails(userId);
        String ogrn = user.orElseThrow()
                .getEmployments()
                .stream()
                .filter(e -> e.getRoles().contains(role.getValue()))
                .findFirst()
                .orElseThrow()
                .getOgrn();

        user = Arrays.stream(
                scosAPIService.getUserByFIO(
                        user.orElseThrow().getFirst_name(),
                        user.orElseThrow().getLast_name()
                ).orElseThrow().getData())
                .filter(u -> u.getUser_id().equals(userId))
                .findFirst();

        Optional<OrganizationDTO> userOrganization =
                scosAPIService.getOrganization(
                        ogrn
                );

        return Optional.of(
                UserUtils.getUserProfileDTOFromUserDTO(
                        user.orElseThrow(),
                        role,
                        userOrganization.orElseThrow())
        );
    }

    /**
     * Получить профиль студента
     * @param userId идентификатор СЦОСа студента
     * @return профиль студента
     */
    private Optional<UserProfileDTO> getStudentProfile(String userId) {
        Optional<UserDTO> user = scosAPIService.getUserDetails(userId);
        String userEmail = user.orElseThrow().getEmail();

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
                        userEmail
                );
        Optional<StudentDTO> student = students.orElseThrow().getResults()
                .stream()
                .filter(
                        s -> s.getEmail().equals(userEmail)
                )
                .filter(s -> s.getStudy_year() != null)
                .findFirst();
        if (student.isPresent()) {
            Optional<OrganizationProfileDTO> organization =
                    scosAPIService.getOrganizationByGlobalId(
                            student.get().getOrganization_id()
                    );

            String studNumber = studentCashRepository.findAllByEmailAndScosId(
                            student.get().getEmail(),
                            user.orElseThrow().getUser_id()
                    ).stream().findAny().orElseThrow()
                    .getStudNumber();

            return Optional.of(
                    UserUtils.getUserProfileDTOFromStudentDTO(
                            student.orElseThrow(),
                            organization.orElseThrow(),
                            studNumber,
                            user.orElseThrow().getPhoto_url(),
                            userEmail)
            );
        }

        return Optional.empty();
    }

    /**
     * Получить пользователей организации
     * @param userId идентификатор польователя
     * @return список пользователей из ООВО админа
     */
    @Override
    public Optional<ResponseDTO<UserDTO>> getUsersByOrganization(String userId,
                                                                 Long page,
                                                                 Long pageSize,
                                                                 String search) {
        Optional<UserDTO> user = scosAPIService.getUserDetails(userId);
        Optional<EmploymentDTO> employmentDTO = user
                .orElseThrow()
                .getEmployments()
                .stream()
                .findFirst();

        Optional<StudentsDTO> students = vamAPIService.getStudents(
                "organization_id",
                employmentDTO.orElseThrow().getOgrn()
        );

        List<UserDTO> users = new ArrayList<>();

        for (StudentDTO student : students.orElseThrow().getResults()) {
            UserDTO userDetails = new UserDTO();
            user = Arrays.stream(
                            scosAPIService.getUserByFIO(
                                            student.getName(),
                                            student.getSurname()
                                    ).orElseThrow()
                                    .getData()
                    )
                    .filter(
                            u -> u.getUser_id().equals(
                                    scosAPIService.getUserByEmail(
                                                    student.getEmail()
                                            )
                                            .orElseThrow()
                                            .getUser_id()
                            )
                    )
                    .findFirst();
            if (user.isPresent()) {
                userDetails.setUser_id(user.get().getUser_id());
                userDetails.setFirst_name(student.getName());
                userDetails.setLast_name(student.getSurname());
                userDetails.setPatronymic_name(student.getMiddle_name());
                userDetails.setEmail(student.getEmail());
                userDetails.setRoles(List.of("STUDENT"));
                userDetails.setPhoto_url(user.get().getPhoto_url());
                userDetails.setUserOrganizationShortName(
                        scosAPIService.getOrganizationByGlobalId(
                                student.getOrganization_id()
                        ).orElseThrow().getShort_name());
                userDetails.setStud(
                        getStudNumber(
                                user.orElseThrow().getEmail(),
                                user.orElseThrow().getUser_id()
                        )
                );
                users.add(userDetails);
            }
        }

        return Optional.of(
                UserUtils.getUsersWithPaginationAndSearch(users, page, pageSize, search)
        );
    }

    /**
     * Получить студента из кэша по почте и СЦОС id
     * @param email почта студента
     * @param scosId идентификатор в СЦОСе
     * @return студент, если не найден - Optional.empty()
     */
    private Optional<CacheStudent> getStudentFromCacheByEmailAndScosId(String email, String scosId) {
        return studentCashRepository.findAllByEmailAndScosId(email, scosId).stream().findFirst();
    }

    /**
     * Получить номер студенческого билета
     * @param email почта студента
     * @param scosId идентификатор студента из СЦОСа
     * @return номер студенческого билета в виде строки
     */
    private String getStudNumber(String email, String scosId) {
        Optional<CacheStudent> cacheStudent =
                getStudentFromCacheByEmailAndScosId(email, scosId);
        if (cacheStudent.isPresent()) {
            return cacheStudent.get().getStudNumber();
        } else {
            saveStudentInCashByEmailAndScosId(email, scosId);
            return getStudNumber(email, scosId);
        }
    }

    /**
     * Сохранить студента в кэш по почте студента
     * @param email почта студента
     * @param scosId идентификатор в СЦОСе
     */
    private void saveStudentInCashByEmailAndScosId(String email, String scosId) {
        studentCashRepository.save(
                new CacheStudent(email, scosId)
        );
    }

    /**
     * Пометить старые валидации как невалидные каждые сутки.
     */
    @Scheduled(fixedDelay = 1000*60*60*24)
    public void makeOldValidationsInvalid() {
        List<CacheStudent> students =
                studentCashRepository.findAllByValidationDateBefore(
                                LocalDate.now().minusMonths(1)
                );

        students.forEach(student -> student.setValid(false));
        studentCashRepository.saveAll(students);
    }
}
