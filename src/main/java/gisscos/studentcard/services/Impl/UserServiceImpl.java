package gisscos.studentcard.services.Impl;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.dto.UserDTO;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.services.IPassRequestService;
import gisscos.studentcard.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final IPassRequestService passRequestService;

    private final GisScosApiRestClient gisScosApiRestClient;

    @Autowired
    public UserServiceImpl(IPassRequestService passRequestService, GisScosApiRestClient gisScosApiRestClient) {
        this.passRequestService = passRequestService;
        this.gisScosApiRestClient = gisScosApiRestClient;
    }

    @Override
    public List<String> getPermittedOrganizations(UserDTO user) {

        List<String> acceptedOrganizationsUUID = passRequestService.getPassRequestsByUserId(user.getUser_id()).get()
                .stream()
                .filter(passRequest -> passRequest.getStatus() == PassRequestStatus.ACCEPTED)
                .map(PassRequest::getTargetUniversityId)
                .collect(Collectors.toList());
        try {
            acceptedOrganizationsUUID.addAll(user.getUserOrganizationsId());
        }catch(java.lang.IllegalArgumentException exception){
            System.err.println("No such university/UUID is invalid");
        }
        return acceptedOrganizationsUUID;
    }

    @Override
    public String getOrganizationsNamesAsString(UserDTO user) {
        return user.getUserOrganizationsId().stream()
                .map(id -> gisScosApiRestClient.makeGetOrganizationRequest(id).getFull_name()).collect(Collectors.joining(", "));
    }

    @Override
    public String getPermittedOrganizationsNamesAsString(UserDTO user) {
        return getPermittedOrganizations(user).stream()
                .map(orgId -> gisScosApiRestClient.makeGetOrganizationRequest(orgId).getFull_name())
                .collect(Collectors.joining(", "));
    }

    @Override
    public String getUserRolesAsString(UserDTO user) {
        return String.join(", ", user.getRoles());
    }

}
