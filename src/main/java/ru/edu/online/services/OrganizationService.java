package ru.edu.online.services;

import ru.edu.online.entities.dto.StudentDTO;

import java.util.List;

public interface OrganizationService {

     List<String> getPermittedOrganizations(StudentDTO studentDTO);

}
