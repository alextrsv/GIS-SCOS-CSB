package ru.edu.online.services.Impl;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.edu.online.clients.GisScosApiRestClient;
import ru.edu.online.entities.DynamicQRUser;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.OrganizationInQRDTO;
import ru.edu.online.entities.dto.PermanentUserQRDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.interfaces.QRUser;
import ru.edu.online.services.IDynamicQRUserService;
import ru.edu.online.services.QRUserService;
import ru.edu.online.utils.HashingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements QRUserService {

    private final GisScosApiRestClient gisScosApiRestClient;

    private final IDynamicQRUserService dynamicQRUserService;


    @Autowired
    public UserServiceImpl(GisScosApiRestClient gisScosApiRestClient,
                           IDynamicQRUserService dynamicQRUserService) {
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.dynamicQRUserService = dynamicQRUserService;
    }



    //////////////////////////////////////////////////////////

    @Override
    public String getFullStaticQRPayload(QRUser qrUser) {
        UserDTO user = (UserDTO) qrUser;

        Optional<OrganizationDTO> organizationDTO = getOrganization(user);
        organizationDTO.flatMap(OrganizationDTO::getOrganizationId).ifPresent(user::setOrganizationID);

        PermanentUserQRDTO permanentUserQRDTO = new PermanentUserQRDTO();

        permanentUserQRDTO.setUserId(String.valueOf(user.getUser_id()));
        permanentUserQRDTO.setSurname(user.getLast_name());
        permanentUserQRDTO.setName(user.getFirst_name());
        permanentUserQRDTO.setMiddle_name(user.getPatronymic_name());
        organizationDTO.ifPresentOrElse(organization -> permanentUserQRDTO.setOrganization(organization.getShort_name()),
                () -> permanentUserQRDTO.setOrganization(""));
        permanentUserQRDTO.setStatus("status");
        permanentUserQRDTO.setRole(getUserRolesAsString(user));
        permanentUserQRDTO.setAccessed_organizations(getDPermittedOrgs(user));

        Gson g = new Gson();
        String content = g.toJson(permanentUserQRDTO);

        System.out.println(content);
        return content;
    }

    @Override
    public String getAbbreviatedStaticQRPayload(QRUser qrUser) {
        return null;
    }

    @SneakyThrows
    @Override
    public String getHash(QRUser qrUser) {
        return HashingUtil.getHash(getFullStaticQRPayload(qrUser));
    }
    ////////////////////////////////////////////////////////////////////////////////



    public String getOrganizationsName(UserDTO userDTO) {
        return gisScosApiRestClient.makeGetOrganizationRequest(userDTO.getUserOrganizationORGN().get(0)).get().getShort_name();
    }

    private List<OrganizationInQRDTO> getDPermittedOrgs(UserDTO userDTO){

        List<OrganizationInQRDTO> orgs = new ArrayList<>();

        dynamicQRUserService.getAcceptedPassRequests(new DynamicQRUser(userDTO))
                .forEach(passRequest -> {
                    orgs.add(new OrganizationInQRDTO(passRequest.getTargetUniversityName(), "",
                            passRequest.getStartDate().toString() + " - " + passRequest.getEndDate().toString()));
                });
        return orgs;
    }


    public String getUserRolesAsString(UserDTO user) {
        return String.join(", ", user.getRoles());
    }

    private Optional<OrganizationDTO> getOrganization(UserDTO userDTO){
        if (userDTO.getUserOrganizationORGN().size() < 1) return Optional.empty();
        return gisScosApiRestClient.makeGetOrganizationByOrgnRequest(userDTO.getUserOrganizationORGN().get(0));
    }

}
