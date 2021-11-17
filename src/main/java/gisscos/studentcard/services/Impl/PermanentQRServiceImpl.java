package gisscos.studentcard.services.Impl;

import gisscos.studentcard.clients.GisScosApiRestClient;
import gisscos.studentcard.clients.VamRestClient;
import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.dto.StudyPlanDTO;
import gisscos.studentcard.services.IPassRequestService;
import gisscos.studentcard.services.IPermanentQRService;
import gisscos.studentcard.services.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * сервис для работы со статическими QR
 */
@Service
public class PermanentQRServiceImpl implements IPermanentQRService {

    private final VamRestClient vamRestClient;

    private final GisScosApiRestClient gisScosApiRestClient;

    private final IPassRequestService passRequestService;

    private final OrganizationService organizationService;

    @Autowired
    public PermanentQRServiceImpl(VamRestClient vamRestClient, GisScosApiRestClient gisScosApiRestClient, IPassRequestService passRequestService, OrganizationService organizationService) {
        this.vamRestClient = vamRestClient;
        this.gisScosApiRestClient = gisScosApiRestClient;
        this.passRequestService = passRequestService;
        this.organizationService = organizationService;
    }

    @Override
    public Optional<Resource> downloadQRAsFile(UUID userId) {

        String content = makeInfoString(userId);
        BufferedImage qrCodeImage = QrGenerator.generateQRCodeImage(content);
        return Converter.getResource(qrCodeImage);
    }


    private String makeInfoString(UUID id){
        StudentDTO studentDTO = vamRestClient.makeGetStudentRequest(id);

        String content = new String ("surname: " + studentDTO.getSurname() +
                "\nname: " + studentDTO.getName() +
                "\nmiddle-name: " + studentDTO.getMiddle_name() +
                "\norganization: " + getOrganizationName(UUID.fromString(studentDTO.getOrganization_id())) +
                "\nstatus: " + "" +
                "\nrole: " + "" +
                "\nstud-bilet: " + "scos" + id.toString().substring(0, 6) +
                "\neducation_form: " +  "gis- error" + //getEducationForm(studentDTO.getStudy_plans()) +
                "\nstart_year: " + getCurrentStudyPlan(studentDTO.getStudy_plans()).get().getStart_year() +
                "\nstud-bilet-duration: " +  getCurrentStudyPlan(studentDTO.getStudy_plans()).get().getEnd_year() +
                "\naccessed organizations: " + getPermittedOrganizationsAsString(studentDTO));
        System.out.println(content);
        return content;
    }

    private String getPermittedOrganizationsAsString(StudentDTO studentDTO) {

        String str =  organizationService.getPermittedOrganizations(studentDTO).stream()
                .map(orgId -> gisScosApiRestClient.makeGetOrganizationRequest(orgId).getFull_name())
                .collect(Collectors.joining(", "));
        str += gisScosApiRestClient.makeGetOrganizationRequest(UUID.fromString(studentDTO.getOrganization_id())).getFull_name();
        System.out.println(str);
        return str;
    }


    private String getEducationForm(List<StudyPlanDTO> studyPlanDTOS) {
        Optional<StudyPlanDTO> studyPlanDTO = getCurrentStudyPlan(studyPlanDTOS);
        if (studyPlanDTO.isPresent()) return vamRestClient.makeGetStudyPlanRequest(studyPlanDTO.get().getId()).getEducation_form();
        else return "";
    }

    private Optional<StudyPlanDTO> getCurrentStudyPlan(List<StudyPlanDTO> studyPlanDTOS){

        for (StudyPlanDTO plan: studyPlanDTOS) {
            int currentYear = 2019; //LocalDateTime.now().getYear();
            if (Integer.parseInt(plan.getStart_year()) <= currentYear &&
            Integer.parseInt(plan.getEnd_year()) >= currentYear)
                return Optional.of(plan);
        }
        return Optional.empty();
    }
    private String getOrganizationName(UUID organization_id) {
        return gisScosApiRestClient.makeGetOrganizationRequest(organization_id).getFull_name();
    }


}
