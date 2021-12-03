package ru.edu.online.entities.dto;

import lombok.Data;

/**
 * Профиль организации для фронта
 */
@Data
public class OrganizationProfileDTO {

    private String global_id;
    private String short_name;
    private String full_name;
    private String site_url;
    private String description;
    private String image_url;
}
