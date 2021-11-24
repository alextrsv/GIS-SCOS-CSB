package ru.edu.online.entities.dto;

import lombok.Data;

/**
 * Профиль организации для фронта
 */
@Data
public class OrganizationProfileDTO {

    private String shortName;
    private String longName;
    private String link;
    private String description;
}
