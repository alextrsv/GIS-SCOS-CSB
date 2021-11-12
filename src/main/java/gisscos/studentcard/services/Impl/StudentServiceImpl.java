package gisscos.studentcard.services.Impl;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.services.IPassRequestService;
import gisscos.studentcard.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final IPassRequestService passRequestService;

    final
    GisScosApiRestClient gisScosApiRestClient;

    @Autowired
    public StudentServiceImpl(IPassRequestService passRequestService, GisScosApiRestClient gisScosApiRestClient) {
        this.passRequestService = passRequestService;
        this.gisScosApiRestClient = gisScosApiRestClient;
    }

    @Override
    public List<String> getPermittedOrganizations(StudentDTO studentDTO) {

        List<String> acceptedOrganizationsUUID = passRequestService.getPassRequestsByUserId(studentDTO.getId()).get()
                .stream()
                .filter(passRequest -> passRequest.getStatus() == PassRequestStatus.ACCEPTED)
                .map(PassRequest::getTargetUniversityId)
                .collect(Collectors.toList());
        try {
            acceptedOrganizationsUUID.add(studentDTO.getOrganization_id());
        }catch(java.lang.IllegalArgumentException exception){
            System.err.println("No such university/UUID is invalid");
        }
        return acceptedOrganizationsUUID;
    }

    @Override
    public String getOrganizationsName(StudentDTO studentDTO) {
        return gisScosApiRestClient.makeGetOrganizationRequest(studentDTO.getOrganization_id()).getFull_name();
    }

    @Override
    public String getPermittedOrganizationsNamesAsString(StudentDTO studentDTO) {
        return getPermittedOrganizations(studentDTO).stream()
                .map(orgId -> gisScosApiRestClient.makeGetOrganizationRequest(orgId).getFull_name())
                .collect(Collectors.joining(", "));
    }
}
