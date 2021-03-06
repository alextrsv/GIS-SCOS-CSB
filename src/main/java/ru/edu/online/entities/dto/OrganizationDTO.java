package ru.edu.online.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * dto - объект организации в СЦОСе
 */
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

    public OrganizationDTO(String short_name, String full_name, String inn, String ogrn, String kpp) {
        this.short_name = short_name;
        this.full_name = full_name;
        this.inn = inn;
        this.ogrn = ogrn;
        this.kpp = kpp;
    }

    public String getQRInterfaceType() {
        return "wiegand-34";
    }
}
