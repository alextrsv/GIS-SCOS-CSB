
package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.services.IPassRequestService;
import gisscos.studentcard.services.OrganizationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final IPassRequestService passRequestService;

    public OrganizationServiceImpl(IPassRequestService passRequestService) {
        this.passRequestService = passRequestService;
    }

    @Override
    public List<UUID> getPermittedOrganizations(StudentDTO studentDTO) {
        List<UUID> acceptedOrganizationsUUID = passRequestService.getPassRequestsByUserId(studentDTO.getId()).get()
                .stream()
                .filter(passRequest -> passRequest.getStatus() == PassRequestStatus.ACCEPTED)
                .map(PassRequest::getTargetUniversityId)
                .collect(Collectors.toList());
        acceptedOrganizationsUUID.add(UUID.fromString(studentDTO.getOrganization_id()));
        return acceptedOrganizationsUUID;
    }
}