package ru.edu.online.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.edu.online.entities.QRUser;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO extends QRUser {

    private String user_id;
    private String email;
    private String snils;
    private String last_name;
    private String first_name;
    private String patronymic_name;
    private List<EmploymentDTO> employments;
    private List<String> roles;
    private String organizationID; //не принимается из СЦОС, устанавливается в UserServiceImpl


    public List<String> getUserOrganizationORGN(){
        List<String> res = employments.stream().map(EmploymentDTO::getOgrn).collect(Collectors.toList());
        for (String str: res) {
            System.out.println(str);
        }
        return res;
    }

}
