package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.services.OrganizationService;
import gisscos.studentcard.services.PassRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final PassRequestService passRequestService;

    public OrganizationServiceImpl(PassRequestService passRequestService) {
        this.passRequestService = passRequestService;
    }

    @Override
    public List<UUID> getPermittedOrganizations(StudentDTO studentDTO) {
        List<UUID> acceptedOrganizationsUUID = passRequestService.getPassRequestsByUserId(studentDTO.getId()).get()
                .stream()
                .filter(passRequest -> passRequest.getStatus() == PassRequestStatus.ACCEPTED)
                .map(PassRequest::getTargetUniversityId)
                .collect(Collectors.toList());
        acceptedOrganizationsUUID.add(studentDTO.getOrganization_id());
        return acceptedOrganizationsUUID;
    }
}
