package gisscos.studentcard.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String user_id;
    private String email;
    private String last_name;
    private String first_name;
    private String patronymic_name;
    private List<EmploymentDTO> employments;
    private List<String> roles;


    public List<String> getUserOrganizationsId(){
        List<String> res = employments.stream().map(employment -> employment.getOgrn().split("_")[0])
                .collect(Collectors.toList());
        for (String str: res) {
            System.out.println(str);
        }
        return res;
    }

}
