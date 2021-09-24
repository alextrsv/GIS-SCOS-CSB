package gisscos.studentcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class StudentCardApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentCardApplication.class, args);
    }

}
