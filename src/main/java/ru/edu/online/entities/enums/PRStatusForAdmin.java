package ru.edu.online.entities.enums;

import java.util.stream.Stream;

/**
 * Статусы заявок для получения администратору ООВО.
 * FOR_PROCESSING   - для обработки
 * IN_PROCESSING    - в обработке
 * PROCESSED        - обработаны
 * EXPIRED          - просрочены
 * CREATED          - создана
 */
public enum PRStatusForAdmin {
    FOR_PROCESSING("forProcessing"),
    IN_PROCESSING("inProcessing"),
    PROCESSED("processed"),
    EXPIRED("expired"),
    CREATED("created")
    ;

    private final String status;

    private String getStatus() {
        return this.status;
    }

    PRStatusForAdmin(String status) {
        this.status = status;
    }

    public static PRStatusForAdmin of(String status) {
        return Stream.of(PRStatusForAdmin.values())
                .filter(s -> s.getStatus().equals(status))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
