package gisscos.studentcard.entities.enums;

/** Возможные типы файлов, прикрепленных к заявке
 * TXT - текстовый файл
 * JPG - изображение формата  JPG
 * JPEG - изображение формата  JPEG
 * PNG - изображение формата  PNG
 * **/

public enum PassFileType {
    TXT("txt"),
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    PDF("pdf"),
    UNDEFINED("")
    ;

    String typeAsString;

    PassFileType(){
    }

    PassFileType(String typeAsString){
        this.typeAsString = typeAsString;
    }


    @Override
    public String toString() {
        return typeAsString;
    }
}
