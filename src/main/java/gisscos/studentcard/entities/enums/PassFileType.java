package gisscos.studentcard.entities.enums;

import java.net.Proxy;
import java.util.stream.Stream;

/** Возможные типы файлов, прикрепленных к заявке
 * TXT - текстовый файл
 * JPG - изображение формата  JPG
 * JPEG - изображение формата  JPEG
 * PNG - изображение формата  PNG
 * PDF - файл формата PDF
 * **/

public enum PassFileType {
    TXT("txt"),
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    PDF("pdf"),
    UNDEFINED("")
    ;

    String type;

    PassFileType(){
    }

    PassFileType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static PassFileType of(String type) {
        return Stream.of(PassFileType.values())
                .filter(t -> t.getType().equals(type))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
