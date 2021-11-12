package gisscos.studentcard.services.Impl;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.dto.StudyPlanDTO;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.services.IPassRequestService;
import gisscos.studentcard.services.IStudentService;
import gisscos.studentcard.utils.HashingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements IStudentService {

    private final IPassRequestService passRequestService;

    final
    GisScosApiRestClient gisScosApiRestClient;

    final
    VamRestClient vamRestClient;

    @Autowired
    public StudentServiceImpl(IPassRequestService passRequestService, GisScosApiRestClient gisScosApiRestClient, VamRestClient vamRestClient) {
        this.passRequestService = passRequestService;
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.vamRestClient = vamRestClient;
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
        return gisScosApiRestClient.makeGetOrganizationRequest(studentDTO.getOrganization_id()).get().getFull_name();
    }

    @Override
    public String getPermittedOrganizationsNamesAsString(StudentDTO studentDTO) {
        return getPermittedOrganizations(studentDTO).stream()
                .map(orgId -> gisScosApiRestClient.makeGetOrganizationRequest(orgId).get().getFull_name())
                .collect(Collectors.joining(", "));
    }

    @Override
    public String makeContent(StudentDTO studentDTO){
        String finalContent = makeUsefullContent(studentDTO);
        try {
            finalContent += "\nhash: " + HashingUtil.getHash(finalContent);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return finalContent;
    }


    public String makeUsefullContent(StudentDTO studentDTO) {
        String content = ("surname: " + studentDTO.getSurname() +
                "\nname: " + studentDTO.getName() +
                "\nmiddle-name: " + studentDTO.getMiddle_name() +
                "\norganization: " + getOrganizationsName(studentDTO) +
                "\nstatus: " + "status" +
                "\nrole: " + "student" +
                "\nstud-bilet: " + "scos" + studentDTO.getId().toString().substring(0, 6) +
                "\neducation_form: " +  getEducationForm(studentDTO.getStudy_plans()) +
                "\nstart_year: " + getStartYear(studentDTO) +
                "\nstud-bilet-duration: " +  getEndYear(studentDTO) +
                "\naccessed organizations: " + getPermittedOrganizationsNamesAsString(studentDTO));

        System.out.println(content);
        return content;
    }

    private String getStartYear(StudentDTO studentDTO) {
        if(studentDTO.getStudy_plans().size() != 0)
            return getCurrentStudyPlan(studentDTO.getStudy_plans()).get().getStart_year();
        else return "";
    }

    private String getEndYear(StudentDTO studentDTO) {
        if(studentDTO.getStudy_plans().size() != 0)
            return  getCurrentStudyPlan(studentDTO.getStudy_plans()).get().getEnd_year();
        else return "";
    }

    private String getEducationForm(List<StudyPlanDTO> studyPlanDTOS) {
        Optional<StudyPlanDTO> studyPlanDTO = getCurrentStudyPlan(studyPlanDTOS);
        if (studyPlanDTO.isPresent())
            return vamRestClient.makeGetStudyPlanRequest(studyPlanDTO.get().getId()).getEducation_form();
        else return "";
    }

    private Optional<StudyPlanDTO> getCurrentStudyPlan(List<StudyPlanDTO> studyPlanDTOS) {

        for (StudyPlanDTO plan : studyPlanDTOS) {
            int currentYear = 2019; //LocalDateTime.now().getYear();
            if (Integer.parseInt(plan.getStart_year()) <= currentYear &&
                    Integer.parseInt(plan.getEnd_year()) >= currentYear)
                return Optional.of(plan);
        }
        return Optional.empty();
    }
}
