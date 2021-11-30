package ru.edu.online.entities.dto;

import com.google.gson.Gson;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PermanentUserQRDTO {
    String userId;
    String surname;
    String name;
    String middle_name;
    String organization;
    String status;
    String role;
    List<OrganizationInQRDTO> accessed_organizations;

    @Override
    public String toString() {
        Gson p = new Gson();
        return p.toJson(this);
    }
}
