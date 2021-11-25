package ru.edu.online.entities.enums;

/**
 * Статус временного QR-кода.
 * DELETED - QR удален
 * UPDATED - QR обновлен
 * EXPIRED - QR просрочился
 */
public enum QRStatus {
    NEW,
    DELETED,
    UPDATED,
    EXPIRED
}
