package gisscos.studentcard.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private @Setter(AccessLevel.PROTECTED) Long id;

    /** Id университета */
    private Long universityId;

    /** Токен пользователя */
    private String token;

    public User(Long id, String token, Long universityId) {
        this.id = id;
        this.token = token;
        this.universityId = universityId;
    }
}
