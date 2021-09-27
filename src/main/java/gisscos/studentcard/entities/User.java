package gisscos.studentcard.entities.enums;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.University;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;


@Data
@Entity
@NoArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private @Setter(AccessLevel.PROTECTED) Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne()
    @JoinColumn(name = "university_id")
    private University university;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<PassRequest> passRequests;

}
