package gisscos.studentcard.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO {
    String short_name;
    String full_name;
    String inn;
    String ogrn;
    String kpp;
    String qrInterfaceType;

    public String getQRInterfaceType() {
        return "wiegand-34";
    }
}
