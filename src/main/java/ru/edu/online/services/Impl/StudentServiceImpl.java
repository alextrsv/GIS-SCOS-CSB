package ru.edu.online.services.Impl;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.edu.online.clients.GisScosApiRestClient;
import ru.edu.online.clients.VamRestClient;
import ru.edu.online.entities.DynamicQRUser;
import ru.edu.online.entities.dto.OrganizationInQRDTO;
import ru.edu.online.entities.dto.PermanentStudentQRDTO;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.dto.StudyPlanDTO;
import ru.edu.online.services.IDynamicQRUserService;
import ru.edu.online.services.IPassRequestService;
import ru.edu.online.services.IStudentService;
import ru.edu.online.utils.HashingUtil;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public String makeContent(StudentDTO studentDTO){
        String finalContent = makeUsefullContent(studentDTO);
        try {
            String hash = HashingUtil.getHash(finalContent);
            finalContent = finalContent.substring(0, finalContent.length()-1);
            finalContent += ", \"hash\": \"" + hash + "\"}";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("fianl content:");
        System.out.println(finalContent + "\n\n");
        return finalContent;
    }

    private List<OrganizationInQRDTO> getDPermittedOrgs(StudentDTO studentDTO){

        List<OrganizationInQRDTO> orgs = new ArrayList<>();

        dynamicQRUserService.getAcceptedPassRequests(new DynamicQRUser(studentDTO))
                .forEach(passRequest -> {
                    orgs.add(new OrganizationInQRDTO(getOrganizationsName(studentDTO), "",
                            passRequest.getStartDate().toString() + " - " + passRequest.getEndDate().toString()));
                });
        return orgs;
    }

    public String makeUsefullContent(StudentDTO studentDTO) {
        PermanentStudentQRDTO permanentStudentQRDTO = new PermanentStudentQRDTO();

        permanentStudentQRDTO.setUserId(String.valueOf(studentDTO.getId())); // id из ВАМ
        permanentStudentQRDTO.setSurname(studentDTO.getSurname());
        permanentStudentQRDTO.setName(studentDTO.getName());
        permanentStudentQRDTO.setMiddle_name( studentDTO.getMiddle_name());
        permanentStudentQRDTO.setOrganization(getOrganizationsName(studentDTO));  //by organizationId
        permanentStudentQRDTO.setStatus("status");
        permanentStudentQRDTO.setRole("student");
        permanentStudentQRDTO.setStud_bilet(studentDTO.getId().toString().substring(0, 10));
        permanentStudentQRDTO.setEducation_form(getEducationForm(studentDTO.getStudy_plans()));
        permanentStudentQRDTO.setStart_year(getStartYear(studentDTO));
        permanentStudentQRDTO.setStud_bilet_duration(getEndYear(studentDTO));
        permanentStudentQRDTO.setAccessed_organizations(getDPermittedOrgs(studentDTO));

        Gson p = new Gson();

        String content = p.toJson(permanentStudentQRDTO);
        System.out.println("CONTENT:");
        System.out.println(content+ "\n\n");
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
