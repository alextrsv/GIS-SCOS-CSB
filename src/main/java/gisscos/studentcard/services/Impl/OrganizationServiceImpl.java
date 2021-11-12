//package gisscos.studentcard.services.Impl;
//
//import gisscos.studentcard.entities.PassRequest;
//import gisscos.studentcard.entities.dto.StudentDTO;
//import gisscos.studentcard.entities.enums.PassRequestStatus;
//import gisscos.studentcard.services.IPassRequestService;
//import gisscos.studentcard.services.OrganizationService;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class OrganizationServiceImpl implements OrganizationService {
//
//    private final IPassRequestService passRequestService;
//
//    public OrganizationServiceImpl(IPassRequestService passRequestService) {
//        this.passRequestService = passRequestService;
//    }
//
//    @Override
//    public synchronized List<String> getStudentPermittedOrganizations(StudentDTO studentDTO) {
//
//        List<String> acceptedOrganizationsUUID = passRequestService.getPassRequestsByUserId(user.getId()).get()
//                .stream()
//                .filter(passRequest -> passRequest.getStatus() == PassRequestStatus.ACCEPTED)
//                .map(PassRequest::getTargetUniversityId)
//                .collect(Collectors.toList());
//        try {
//            acceptedOrganizationsUUID.add(user.getOrganization_id());
//        }catch(java.lang.IllegalArgumentException exception){
//            System.err.println("No such university/UUID is invalid");
//        }
//        return acceptedOrganizationsUUID;
//    }
//
//}
