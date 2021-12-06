
package ru.edu.online.services.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.OrganizationProfileDTO;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.enums.PassRequestStatus;
import ru.edu.online.services.IOrganizationService;
import ru.edu.online.services.IPassRequestService;
import ru.edu.online.services.IScosAPIService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl implements IOrganizationService {

    /** Сервис заявок */
    private final IPassRequestService passRequestService;

    /** Сервис для работы с АПИ СЦОСа */
    private final IScosAPIService scosAPIService;

    @Autowired
    public OrganizationServiceImpl(IPassRequestService passRequestService,
                                   IScosAPIService scosAPIService) {
        this.passRequestService = passRequestService;
        this.scosAPIService = scosAPIService;
    }

    @Override
    public List<String> getPermittedOrganizations(StudentDTO studentDTO) {
        List<String> acceptedOrganizationsUUID =
                passRequestService.getPassRequestsByUserId(
                                studentDTO.getId()
                        ).orElseThrow()
                        .stream()
                        .filter(passRequest -> passRequest.getStatus() == PassRequestStatus.ACCEPTED)
                        .map(PassRequest::getTargetUniversityId)
                        .collect(Collectors.toList());
        acceptedOrganizationsUUID.add(studentDTO.getOrganization_id());
        return acceptedOrganizationsUUID;
    }

    /**
     * Получить список организаций
     * @return мапа: global_id - короткое название организации
     */
    @Override
    public Map<String, String> getOrganizations() {
        OrganizationDTO[] organizations = scosAPIService.getOrganizations().orElseThrow();
        Map<String, String> organizationsForUI = new HashMap<>();

        for (OrganizationDTO organization : organizations) {
            if (organization.getOrganizationId().isPresent()) {
                organizationsForUI.put(organization.getOrganizationId().get(), organization.getShort_name());
            }
        }

        return organizationsForUI;
    }

    @Override
    public Optional<OrganizationProfileDTO> getOrganizationProfile(String globalId) {
        return scosAPIService.getOrganizationByGlobalId(globalId);
    }
}
