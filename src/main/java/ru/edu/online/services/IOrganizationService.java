package ru.edu.online.services;

import ru.edu.online.entities.dto.StudentDTO;

import java.util.List;
import java.util.Map;

public interface IOrganizationService {

     List<String> getPermittedOrganizations(StudentDTO studentDTO);

     Map<String, String> getOrganizations();
}
