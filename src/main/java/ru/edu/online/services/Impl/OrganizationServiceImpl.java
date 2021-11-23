
package ru.edu.online.services.Impl;

import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.enums.PassRequestStatus;
import ru.edu.online.services.IPassRequestService;
import ru.edu.online.services.OrganizationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final IPassRequestService passRequestService;

    public OrganizationServiceImpl(IPassRequestService passRequestService) {
        this.passRequestService = passRequestService;
    }

    @Override
    public List<String> getPermittedOrganizations(StudentDTO studentDTO) {
        List<String> acceptedOrganizationsUUID = passRequestService.getPassRequestsByUserId(studentDTO.getId()).get()
                .stream()
                .filter(passRequest -> passRequest.getStatus() == PassRequestStatus.ACCEPTED)
                .map(PassRequest::getTargetUniversityId)
                .collect(Collectors.toList());
        acceptedOrganizationsUUID.add(studentDTO.getOrganization_id());
        return acceptedOrganizationsUUID;
    }
}