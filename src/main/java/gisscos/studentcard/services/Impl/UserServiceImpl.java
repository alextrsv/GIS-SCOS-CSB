package gisscos.studentcard.services.Impl;

import com.google.gson.Gson;
import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.entities.DynamicQRUser;
import gisscos.studentcard.entities.dto.OrganizationInQRDTO;
import gisscos.studentcard.entities.dto.PermanentUserQRDTO;
import gisscos.studentcard.entities.dto.UserDTO;
import gisscos.studentcard.services.IDynamicQRUserService;
import gisscos.studentcard.services.IPassRequestService;
import gisscos.studentcard.services.IUserService;
import gisscos.studentcard.utils.HashingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {

    private final IPassRequestService passRequestService;

    private final GisScosApiRestClient gisScosApiRestClient;

    private final IDynamicQRUserService dynamicQRUserService;

    @Autowired
    public UserServiceImpl(IPassRequestService passRequestService, GisScosApiRestClient gisScosApiRestClient, IDynamicQRUserService dynamicQRUserService) {
        this.passRequestService = passRequestService;
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.dynamicQRUserService = dynamicQRUserService;
    }

    @Override
    public String getOrganizationsNamesAsString(UserDTO user) {
        return user.getUserOrganizationsId().stream()
                .map(id -> gisScosApiRestClient.makeGetOrganizationRequest(id).get().getShort_name()).collect(Collectors.joining(", "));
    }

    public String getOrganizationsName(UserDTO userDTO) {
        return gisScosApiRestClient.makeGetOrganizationRequest(userDTO.getUserOrganizationsId().get(0)).get().getShort_name();
    }

    private List<OrganizationInQRDTO> getDPermittedOrgs(UserDTO userDTO){

        List<OrganizationInQRDTO> orgs = new ArrayList<>();

        dynamicQRUserService.getAcceptedPassRequests(new DynamicQRUser(userDTO))
                .forEach(passRequest -> {
                    orgs.add(new OrganizationInQRDTO(getOrganizationsName(userDTO), "",
                            passRequest.getStartDate().toString() + " - " + passRequest.getEndDate().toString()));
                });
        return orgs;
    }

    @Override
    public String getUserRolesAsString(UserDTO user) {
        return String.join(", ", user.getRoles());
    }


    @Override
    public String makeContent(UserDTO userDTO){
        String finalContent = makeUsefullContent(userDTO);
        try {
            finalContent = finalContent.substring(0, finalContent.length()-1);
            finalContent += ", \"hash\": \"" + HashingUtil.getHash(finalContent) + "\"}";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return finalContent;
    }

    @Override
    public String makeUsefullContent(UserDTO user) {

        PermanentUserQRDTO permanentUserQRDTO = new PermanentUserQRDTO();

        permanentUserQRDTO.setUserId(String.valueOf(user.getUser_id()));
        permanentUserQRDTO.setSurname(user.getLast_name());
        permanentUserQRDTO.setName(user.getFirst_name());
        permanentUserQRDTO.setMiddle_name(user.getPatronymic_name() );
        permanentUserQRDTO.setOrganization(getOrganizationsNamesAsString(user));
        permanentUserQRDTO.setStatus("status");
        permanentUserQRDTO.setRole(getUserRolesAsString(user));
        permanentUserQRDTO.setAccessed_organizations(getDPermittedOrgs(user));

        Gson g = new Gson();
        String content = g.toJson(permanentUserQRDTO);

        System.out.println(content);
        return content;
    }

}
