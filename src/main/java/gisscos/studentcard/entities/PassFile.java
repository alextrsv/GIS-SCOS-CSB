package gisscos.studentcard.entities;

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

    //TODO Добавить поле, описывающее заявку, к которой прикреплен файл

    /** Id файла, прикрепленного к заявке в БД. Генерируется автоматически */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private @Setter(AccessLevel.PROTECTED) Long id;

    /** Имя файла **/
    private String name;
    /** Тип файла **/
    private PassFileType type;
    /** Путь к файлу **/
    private String path;



    public PassFile(String name, PassFileType type, String path){
        this.name = name;
        this.type = type;
        this.path = path;
    }
}
