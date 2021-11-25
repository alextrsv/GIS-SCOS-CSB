package ru.edu.online.entities.enums;

import java.util.stream.Stream;

/**
 * Статусы заявок для администратора.
 * FOR_PROCESSING - для обработки
 * IN_PROCESSING - в обработке
 * PROCESSED - обработаны
 */
public enum RequestsStatusForAdmin {
    FOR_PROCESSING("forProcessing"),
    IN_PROCESSING("inProcessing"),
    PROCESSED("processed")
    ;

    private final String status;

    private String getStatus() {
        return this.status;
    }

    RequestsStatusForAdmin(String status) {
        this.status = status;
    }

    public static RequestsStatusForAdmin of(String status) {
        return Stream.of(RequestsStatusForAdmin.values())
                .filter(s -> s.getStatus().equals(status))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
