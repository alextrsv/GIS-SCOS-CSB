package gisscos.studentcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gisscos.studentcard.entities.enums.PassFileType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
@NoArgsConstructor
public class PassFile {

    /** Id файла, прикрепленного к заявке в БД. Генерируется автоматически */
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Имя файла **/
    private String name;
    /** Тип файла **/
    private PassFileType type;
    /** Путь к файлу **/
    @JsonIgnore
    private String path;
    /** Идентификатор заявки, к которой прикреплён файл */
    private Long passRequestId;



    public PassFile(String name, PassFileType type,
                    String path, Long passRequestId){
        this.name = name;
        this.type = type;
        this.path = path;
        this.passRequestId = passRequestId;
    }
}
