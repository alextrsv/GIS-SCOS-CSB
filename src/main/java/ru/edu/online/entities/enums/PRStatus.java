package ru.edu.online.entities.enums;

import java.util.stream.Stream;

/**
 * Статус заявки.
 * WAITING_FOR_APPROVEMENT_BY_USER - ожидает одобрения пользователем.
 * TARGET_ORGANIZATION_REVIEW - отправлена на рассмотрение в целевую ООВО.
 * PROCESSED_IN_TARGET_ORGANIZATION - обрабатывается целевой ООВО.
 * REJECTED_BY_TARGET_ORGANIZATION - отклонена целевой ООВО.
 * EXPIRED - истек срок действия заявки. (текущая дата больше конца периода заявки)
 * ACCEPTED - одобрена
 */
public enum PRStatus {
    WAITING_FOR_APPROVEMENT_BY_USER,
    TARGET_ORGANIZATION_REVIEW,
    PROCESSED_IN_TARGET_ORGANIZATION,
    REJECTED_BY_TARGET_ORGANIZATION,
    EXPIRED,
    ACCEPTED
    ;

    public static PRStatus of(String status) {
        return Stream.of(PRStatus.values())
                .filter(t -> t.toString().equals(status))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
