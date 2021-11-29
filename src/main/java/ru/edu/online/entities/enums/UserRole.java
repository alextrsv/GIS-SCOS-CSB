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
    ADMIN,
    SECURITY,
    STUDENT,
    TEACHER,
    SUPER_USER,
    UNDEFINED
}
