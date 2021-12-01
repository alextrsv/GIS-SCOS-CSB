package ru.edu.online.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.edu.online.entities.dto.OrganizationProfileDTO;
import ru.edu.online.services.IOrganizationService;

import java.util.Map;

@RestController
@RequestMapping("/organization")
public class OrganizationController {

    private final IOrganizationService organizationService;

    @Autowired
    public OrganizationController(IOrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Получить список организаций
     * @return мапа: global_id - короткое название организации
     */
    @GetMapping("/list")
    public Map<String, String> getOrganizations() {
        return organizationService.getOrganizations();
    }

    /**
     * Получить профиль организации для фронта
     * @param id идентификатор организации
     * @return профиль организации
     */
    @GetMapping("/profile")
    public ResponseEntity<OrganizationProfileDTO> getOrganizationProfile(@RequestParam String id) {
        return ResponseEntity.of(organizationService.getOrganizationProfile(id));
    }
}
