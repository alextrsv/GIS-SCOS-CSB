package gisscos.studentcard.entities;

import gisscos.studentcard.entities.enums.PassFileType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@Table(name = "pass_file")
public class PassFile {

    //TODO Добавить поле, описывающее заявку, к которой прикреплен файл

    /** Id файла, прикрепленного к заявке в БД. Генерируется автоматически */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private @Setter(AccessLevel.PROTECTED) Long id;

    /** Имя файла **/
    @Column(name = "name")
    private String name;
    /** Тип файла **/
    @Column(name = "type")
    private PassFileType type;

    @Column(name = "path")
    /** Путь к файлу **/
    private @Getter String path;

    @ManyToOne()
    @JoinColumn(name = "pass_request_id")
    private PassRequest passRequest;



    public PassFile(String name, PassFileType type, String path){
        this.name = name;
        this.type = type;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
