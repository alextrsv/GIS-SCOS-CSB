package gisscos.studentcard.services.Impl;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.DynamicQRUser;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.dto.StudyPlanDTO;
import gisscos.studentcard.services.IDynamicQRUserService;
import gisscos.studentcard.services.IPassRequestService;
import gisscos.studentcard.services.IStudentService;
import gisscos.studentcard.utils.HashingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements IStudentService {

    private final IPassRequestService passRequestService;

    private final GisScosApiRestClient gisScosApiRestClient;

    private final VamRestClient vamRestClient;

    private final IDynamicQRUserService dynamicQRUserService;

    @Autowired
    public StudentServiceImpl(IPassRequestService passRequestService, GisScosApiRestClient gisScosApiRestClient, VamRestClient vamRestClient, IDynamicQRUserService dynamicQRUserService) {
        this.passRequestService = passRequestService;
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.vamRestClient = vamRestClient;
        this.dynamicQRUserService = dynamicQRUserService;
    }

    @Override
    public Set<String> getPermittedOrganizations(StudentDTO studentDTO) {
        return dynamicQRUserService.getPermittedOrganizations(new DynamicQRUser(studentDTO));
    }

    @Override
    public String getOrganizationsName(StudentDTO studentDTO) {
        return gisScosApiRestClient.makeGetOrganizationRequest(studentDTO.getOrganization_id()).get().getShort_name();
    }

    @Override
    public String getPermittedOrganizationsNamesAsString(StudentDTO studentDTO) {
        return getPermittedOrganizations(studentDTO).stream()
                .map(orgId -> gisScosApiRestClient.makeGetOrganizationRequest(orgId).get().getShort_name())
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
