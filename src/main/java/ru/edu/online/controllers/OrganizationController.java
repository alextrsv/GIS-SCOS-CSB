package ru.edu.online.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
     * @return мапа: ОГРН - короткое название организации
     */
    @GetMapping("/list")
    public Map<String, String> getOrganizations() {
        return organizationService.getOrganizations();
    }
}
