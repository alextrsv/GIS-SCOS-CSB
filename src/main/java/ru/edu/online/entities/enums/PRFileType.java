package ru.edu.online.entities.enums;

import org.springframework.http.MediaType;

import java.util.stream.Stream;

/** Возможные типы файлов, прикрепленных к заявке
 * TXT - текстовый файл
 * JPG - изображение формата  JPG
 * JPEG - изображение формата  JPEG
 * PNG - изображение формата  PNG
 * PDF - файл формата PDF
 * **/
public enum PRFileType {
    DOC("doc"),
    DOCX("docx"),
    TXT("txt"),
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    PDF("pdf"),
    UNDEFINED("")
    ;

    String type;

    PRFileType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * Получение MediaType по значению
     * @param type один из типов файла
     * @return MediaType (Mime type)
     */
    public static MediaType getMediaType(PRFileType type) {
        switch (type) {
            case DOC:
            case DOCX:
                return MediaType.APPLICATION_OCTET_STREAM;
            case TXT:
                return MediaType.TEXT_PLAIN;
            case JPG:
            case JPEG:
                return MediaType.IMAGE_JPEG;
            case PNG:
                return MediaType.IMAGE_PNG;
            case PDF:
                return MediaType.APPLICATION_PDF;
            default:
                return MediaType.ALL;
        }
    }

    public static PRFileType of(String type) {
        return Stream.of(PRFileType.values())
                .filter(t -> t.getType().equals(type))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
