package gisscos.studentcard.entities.enums;

import java.util.stream.Stream;

/**
 * Статус заявки.
 * WAITING_FOR_APPROVEMENT_BY_USER - ожидает одобрения пользователем.
 * TARGET_ORGANIZATION_REVIEW - отправлена на рассмотрение в целевую ООВО.
 * PROCESSED_IN_TARGET_ORGANIZATION - обрабатывается целевой ООВО.
 * REJECTED_BY_TARGET_ORGANIZATION - отклонена целевой ООВО.
 * CANCELED_BY_CREATOR - отменена создателем.
 * EXPIRED - истек срок действия заявки. (текущая дата больше конца периода заявки)
 * ACCEPTED - одобрена
 */
public enum PassRequestStatus {
    WAITING_FOR_APPROVEMENT_BY_USER,
    TARGET_ORGANIZATION_REVIEW,
    PROCESSED_IN_TARGET_ORGANIZATION,
    REJECTED_BY_TARGET_ORGANIZATION,
    CANCELED_BY_CREATOR,
    EXPIRED,
    ACCEPTED
    ;

    public static PassRequestStatus of(String status) {
        return Stream.of(PassRequestStatus.values())
                .filter(t -> t.toString().equals(status))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
