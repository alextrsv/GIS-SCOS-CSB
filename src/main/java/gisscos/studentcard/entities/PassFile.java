package gisscos.studentcard.entities;

import gisscos.studentcard.entities.enums.PassFileType;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
public class PassFile {

    //TODO Добавить поле, описывающее заявку, к которой прикреплен файл

    /** Id файла, прикрепленного к заявке в БД. Генерируется автоматически */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private @Setter(AccessLevel.PROTECTED) Long id;

    /** Имя файла **/
    private String name;
    /** Тип файла **/
    private PassFileType type;
    /** размер файла **/
    private Long size;
    /**  Если будет использоваться облачное хранилище - uri файла в этом хранилище**/
    private String uri;
    /** Дата загрузки файла **/
    private LocalDate uploadDate;
//    /** файл **/
//    private byte[] data;

    /** Путь к файлу **/
    private @Getter String path;


    public PassFile(String name, PassFileType type, Long size, String uri,
             LocalDate uploadDate, String path){
        this.name = name;
        this.type = type;
        this.size = size;
        this.uri = uri;
        this.uploadDate = uploadDate;
        this.path = path;
    }
    public PassFile(String name, PassFileType type, Long size,
                    LocalDate uploadDate, String path){
        this.name = name;
        this.type = type;
        this.size = size;
        this.uploadDate = uploadDate;
        this.path = path;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
