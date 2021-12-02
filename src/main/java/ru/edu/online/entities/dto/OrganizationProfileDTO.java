package ru.edu.online.entities.dto;

import lombok.Data;

/**
 * Профиль организации для фронта
 */
@Data
public class OrganizationProfileDTO {

    private String globalId;
    private String shortName;
    private String fullName;
    private String siteUrl;
    private String description;
}
