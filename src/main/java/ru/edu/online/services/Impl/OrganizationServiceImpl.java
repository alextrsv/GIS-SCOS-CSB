
package ru.edu.online.services.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.OrganizationProfileDTO;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.enums.PassRequestStatus;
import ru.edu.online.services.IOrganizationService;
import ru.edu.online.services.IPassRequestService;
import ru.edu.online.utils.ScosApiUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl implements IOrganizationService {

    private final IPassRequestService passRequestService;
    private final WebClient scosApiClient;

    @Autowired
    public OrganizationServiceImpl(IPassRequestService passRequestService,
                                   WebClient devScosApiClient) {
        this.passRequestService = passRequestService;
        this.scosApiClient = devScosApiClient;
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

    /**
     * Получить список организаций
     * @return мапа: ОГРН - короткое название организации
     */
    @Override
    public Map<String, String> getOrganizations() {
        OrganizationDTO[] organizations = ScosApiUtils.getOrganizations(scosApiClient);
        Map<String, String> organizationsForUI = new HashMap<>();

        for (OrganizationDTO organization : organizations) {
            organizationsForUI.put(organization.getOgrn(), organization.getShort_name());
        }

        return organizationsForUI;
    }

    @Override
    public Optional<OrganizationProfileDTO> getOrganizationProfile(String id) {
        OrganizationProfileDTO organizationProfile = new OrganizationProfileDTO();
        Optional<OrganizationDTO> organizationDTO = ScosApiUtils.getOrganization(scosApiClient, id);

        if (organizationDTO.isPresent()) {
            organizationProfile.setShortName(organizationDTO.get().getShort_name());
            organizationProfile.setLongName(organizationDTO.get().getFull_name());
            organizationProfile.setDescription("Описания ООВО у СЦОСа нету :(");
            organizationProfile.setLink("www.google.com");

            return Optional.of(organizationProfile);
        }

        organizationDTO = ScosApiUtils.getOrganizationByGlobalId(scosApiClient, id);

        if (organizationDTO.isPresent()) {
            organizationProfile.setShortName(organizationDTO.get().getShort_name());
            organizationProfile.setLongName(organizationDTO.get().getFull_name());
            organizationProfile.setDescription("Описания ООВО у СЦОСа нету :(");
            organizationProfile.setLink("www.google.com");

            return Optional.of(organizationProfile);
        }

        return Optional.empty();
    }
}
