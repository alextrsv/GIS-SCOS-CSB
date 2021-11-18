package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.DynamicQRUser;
import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.entities.enums.PassRequestType;
import gisscos.studentcard.repositories.IDynamicQRUserRepository;
import gisscos.studentcard.repositories.IPassRequestUserRepository;
import gisscos.studentcard.services.IDynamicQRUserService;
import gisscos.studentcard.services.IPassRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DynamicQRUserServiceImpl implements IDynamicQRUserService {

    final
    IDynamicQRUserRepository dynamicQRUserRepository;

    private final IPassRequestService passRequestService;

    private final IPassRequestUserRepository passRequestUserRepository;

    @Autowired
    public DynamicQRUserServiceImpl(IDynamicQRUserRepository dynamicQRUserRepository, IPassRequestService passRequestService, IPassRequestUserRepository passRequestUserRepository) {
        this.dynamicQRUserRepository = dynamicQRUserRepository;
        this.passRequestService = passRequestService;
        this.passRequestUserRepository = passRequestUserRepository;
    }

    public List<DynamicQRUser> addAll(List<StudentDTO> studentDTOList){

        List<DynamicQRUser> dynamicQRUsersList = new ArrayList<>();
        studentDTOList.forEach(studentDTO -> {
            System.out.println(studentDTO.getId());
            dynamicQRUsersList.add(new DynamicQRUser(studentDTO));
        });

        dynamicQRUserRepository.saveAll(dynamicQRUsersList);

        return dynamicQRUsersList;

    }

    @Override
    public Set<String> getPermittedOrganizations(DynamicQRUser user) {

        return getAcceptedPassRequests(user).stream()
                .map(PassRequest::getTargetUniversityId)
                .collect(Collectors.toSet());
    }

    public Set<PassRequest> getAcceptedPassRequests(DynamicQRUser user){
        /*
        FOR GROUP TYPE
        1. Получить List<PassRequestUser> по userId
        2. Из полученного списка получить список id заявок
        3. Для каждого id заявки получить заявку из бд

        Для SINGLE TYPE
        1. Получить заявки по userId
        2. Отфильртовать их по типу (SINGLE)
        */

        List<PassRequest> acceptedRequestsForUser = passRequestService.getPassRequestsByUserId(user.getUserId()).get()
                .stream()
                .filter(passRequest -> passRequest.getStatus() == PassRequestStatus.ACCEPTED)
                .filter(passRequest -> passRequest.getType() == PassRequestType.SINGLE)
                .collect(Collectors.toList());

        acceptedRequestsForUser.addAll(passRequestUserRepository.getByUserId(user.getUserId()).stream()
                .map(passRequestUser -> passRequestService.getPassRequestById(passRequestUser.getPassRequestId()).get())
                .filter(passRequest -> passRequest.getType() == PassRequestType.GROUP)
                .collect(Collectors.toList()));

        return new LinkedHashSet<>(acceptedRequestsForUser);
    }


}