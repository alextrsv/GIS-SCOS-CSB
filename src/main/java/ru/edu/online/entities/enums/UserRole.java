package ru.edu.online.entities.enums;

/**
 * Роли пользователей на бэкэнде
 * ADMIN      - администратор ООВО
 * SECURITY   - сотрудник службы охраны в ООВО
 * STUDENT    - студент, подтверждённый ВАМ
 * TEACHER    - препод (хардкод)
 * SUPER_USER - администратор СЦОСа
 * UNDEFINED  - роль не определена
 */
public enum UserRole {
    ADMIN("UNIVERSITY"),
    SECURITY("SECURITY_OFFICER"),
    STUDENT("STUDENT"),
    TEACHER("TEACHER"),
    SUPER_USER("SUPER_USER"),
    UNDEFINED("UNDEFINED")
    ;
    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
