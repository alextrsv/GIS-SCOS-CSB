package gisscos.studentcard.entities.enums;

import java.util.stream.Stream;

/**
 * Статус заявки.
 * USER_ORGANISATION_REVIEW - отправлена на рассмотрение в свою ООВО.
 * TARGET_ORGANISATION_REVIEW - отправлена на рассмотрение в целевую ООВО.
 * PROCESSED_IN_TARGET_ORGANIZATION - обрабатывается целевой ООВО.
 * REJECTED_BY_TARGET_ORGANIZATION - отклонена целевой ООВО.
 * CANCELED_BY_CREATOR - отменена создателем.
 * EXPIRED - истек срок действия заявки.
 * ACCEPTED - одобрена
 * (текущая дата больше конца периода заявки)
 */
public enum PassRequestStatus {
    USER_ORGANISATION_REVIEW,
    TARGET_ORGANISATION_REVIEW,
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
