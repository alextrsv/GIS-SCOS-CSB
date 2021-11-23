package ru.edu.online.services.Impl;

import com.google.gson.Gson;
import ru.edu.online.clients.GisScosApiRestClient;
import ru.edu.online.entities.DynamicQRUser;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.OrganizationInQRDTO;
import ru.edu.online.entities.dto.PermanentUserQRDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.services.IDynamicQRUserService;
import ru.edu.online.services.IPassRequestService;
import ru.edu.online.services.IUserService;
import ru.edu.online.utils.HashingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                .map(id -> {
                    Optional<OrganizationDTO> organization =  gisScosApiRestClient.makeGetOrganizationRequest(id);
                    if (organization.isPresent())
                        return gisScosApiRestClient.makeGetOrganizationRequest(id).get().getShort_name();
                    else return "";
                }).collect(Collectors.joining(", "));
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
