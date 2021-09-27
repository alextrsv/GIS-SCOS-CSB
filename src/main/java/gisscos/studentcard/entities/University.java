//package gisscos.studentcard.entities;
//
//import lombok.AccessLevel;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import javax.persistence.*;
//import java.util.Collection;
//
//@Data
//@Entity
//@NoArgsConstructor
//@Table(name = "university")
//public class University {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private @Setter(AccessLevel.PROTECTED) Long id;
//
//    @Column(name = "name")
//    private String name;
//
//    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Collection<User> users;
//
////    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
////    private Collection<PassRequest> passRequests;
//}
