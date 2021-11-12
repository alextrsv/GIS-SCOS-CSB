package gisscos.studentcard.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

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
    GlobalIdDTO global_ids;

    public Optional<String> getOrganizationId(){
        if (global_ids.getInstitution_id().size() != 0)
         return Optional.ofNullable(global_ids.getInstitution_id().get(0));
        else return Optional.empty();
    }

    public String getQRInterfaceType() {
        return "wiegand-34";
    }
}
