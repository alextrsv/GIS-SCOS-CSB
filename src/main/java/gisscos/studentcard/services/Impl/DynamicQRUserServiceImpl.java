package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.DynamicQRUser;
import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.repositories.IDynamicQRUserRepository;
import gisscos.studentcard.services.IDynamicQRUserService;
import gisscos.studentcard.services.IPassRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DynamicQRUserServiceImpl implements IDynamicQRUserService {

    final
    IDynamicQRUserRepository dynamicQRUserRepository;

    private final IPassRequestService passRequestService;

    @Autowired
    public DynamicQRUserServiceImpl(IDynamicQRUserRepository dynamicQRUserRepository, IPassRequestService passRequestService) {
        this.dynamicQRUserRepository = dynamicQRUserRepository;
        this.passRequestService = passRequestService;
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
    public List<String> getPermittedOrganizations(DynamicQRUser user) {

        List<String> acceptedOrganizationsID = passRequestService.getPassRequestsByUserId(user.getUserId()).get()
                .stream()
                .filter(passRequest -> passRequest.getStatus() == PassRequestStatus.ACCEPTED)
                .map(PassRequest::getTargetUniversityId)
                .collect(Collectors.toList());
//        try {
//            acceptedOrganizationsID.add(user.getOrganizationId());
//        }catch(java.lang.IllegalArgumentException exception){
//            System.err.println("No such university/UUID is invalid");
//        }
        return acceptedOrganizationsID;
    }

}
