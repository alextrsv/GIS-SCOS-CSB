package ru.edu.online.services;

import ru.edu.online.entities.dto.OrganizationProfileDTO;
import ru.edu.online.entities.dto.StudentDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IOrganizationService {

     List<String> getPermittedOrganizations(StudentDTO studentDTO);

     Map<String, String> getOrganizations();

     Optional<OrganizationProfileDTO> getOrganizationProfile(String id);
}
